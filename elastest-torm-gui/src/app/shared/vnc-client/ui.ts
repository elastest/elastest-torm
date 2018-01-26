import { Observable, Subject } from 'rxjs/Rx';
import * as Log from '../../../assets/vnc-resources/core/util/logging.js';
import _, { l10n } from '../../../assets/vnc-resources/core/util/localization.js';
import { isTouchDevice, browserSupportsCursorURIs as cursorURIsSupported } from '../../../assets/vnc-resources/core/util/browsers.js';
import { setCapture, getPointerEvent } from '../../../assets/vnc-resources/core/util/events.js';
import KeyTable from '../../../assets/vnc-resources/core/input/keysym.js';
import keysyms from '../../../assets/vnc-resources/core/input/keysymdef.js';
import RFB from '../../../assets/vnc-resources/core/rfb.js';
import Display from '../../../assets/vnc-resources/core/display.js';
import * as WebUtil from '../../../assets/vnc-resources/app/webutil.js';

export class VncUI {
    connected: boolean;
    desktopName: string;
    resizeTimeout;
    statusTimeout;
    hideKeyboardTimeout;
    idleControlbarTimeout;
    closeControlbarTimeout;

    controlbarGrabbed: boolean;
    controlbarDrag: boolean;
    controlbarMouseDownClientY: number;
    controlbarMouseDownOffsetY: number;

    isSafari: boolean;
    rememberedClipSetting;
    lastKeyboardinput;
    defaultKeyboardinputLen: number;

    inhibit_reconnect: boolean;
    reconnect_callback;
    reconnect_password;

    host: string;
    port: any;
    password: string;
    autoconnect: boolean;
    viewOnly: boolean;

    resizeMode: string = 'scale'; // Hardcoded
    public rfb;

    _statusObs: Subject<string> = new Subject<string>();
    statusObs: Observable<string> = this._statusObs.asObservable();

    constructor(host: string, port: any, autoconnect: boolean = false, viewOnly: boolean = false, password: string = undefined) {
        this.connected = false;
        this.desktopName = '';

        this.resizeTimeout = undefined;
        this.statusTimeout = undefined;
        this.hideKeyboardTimeout = undefined;
        this.idleControlbarTimeout = undefined;
        this.closeControlbarTimeout = undefined;

        this.controlbarGrabbed = false;
        this.controlbarDrag = false;
        this.controlbarMouseDownClientY = 0;
        this.controlbarMouseDownOffsetY = 0;

        this.isSafari = false;
        this.rememberedClipSetting = undefined;
        this.lastKeyboardinput = undefined;
        this.defaultKeyboardinputLen = 100;

        this.inhibit_reconnect = true;
        this.reconnect_callback = undefined;
        this.reconnect_password = undefined;

        this.host = host;
        this.port = port;
        this.password = password;
        this.autoconnect = autoconnect;
        this.viewOnly = viewOnly;
    }

    prime(callback?) {
        if (document.readyState === 'interactive' || document.readyState === 'complete') {
            this.load(callback);
        } else {
            document.addEventListener('DOMContentLoaded', this.load.bind(this, callback));
        }
    }

    // Setup rfb object, load settings from browser storage, then call
    // this.init to setup the UI/menus
    load(callback) {
        WebUtil.initSettings(this.start.bind(this));
    }

    // Render default UI and initialize settings menu
    start(callback) {
        // Setup global variables first
        this.isSafari = (navigator.userAgent.indexOf('Safari') !== -1 &&
            navigator.userAgent.indexOf('Chrome') === -1);

        this.initSettings();

        // Translate the DOM
        l10n.translateDOM();

        // Adapt the interface for touch screen devices
        if (isTouchDevice) {
            document.documentElement.classList.add('noVNC_touch');
            // Remove the address bar
            setTimeout(function () { window.scrollTo(0, 1); }, 100);
        }

        // Restore control bar position
        if (WebUtil.readSetting('controlbar_pos') === 'right') {
            this.toggleControlbarSide();
        }

        this.initFullscreen();

        // Setup event handlers
        this.addResizeHandlers();
        this.addControlbarHandlers();
        this.addTouchSpecificHandlers();
        this.addExtraKeysHandlers();
        this.addXvpHandlers();
        this.addConnectionControlHandlers();
        this.addClipboardHandlers();
        this.addSettingsHandlers();

        // Bootstrap fallback input handler
        this.keyboardinputReset();

        this.openControlbar();

        // Show the connect panel on first load unless autoconnecting
        if (!this.autoconnect) {
            this.openConnectPanel();
        }

        this.updateViewClip();

        this.updateVisualState();

        if (document.getElementById('noVNC_setting_host')) {
            document.getElementById('noVNC_setting_host').focus();
        }
        if (document.getElementById('noVNC_loading')) {
            document.documentElement.classList.remove('noVNC_loading');
        }

        if (this.autoconnect) {
            this.connect();
        }

        if (typeof callback === 'function') {
            callback(this.rfb);
        }
    }

    initFullscreen() {
        // Only show the button if fullscreen is properly supported
        // * Safari doesn't support alphanumerical input while in fullscreen
        let fullScreenButton = document.getElementById('noVNC_fullscreen_button');
        if (fullScreenButton === null || fullScreenButton === undefined) { return; }
        let documentElement = document.documentElement as any;
        let documentBody = document.body as any;
        if (!this.isSafari &&
            (documentElement.requestFullscreen ||
                documentElement.mozRequestFullScreen ||
                documentElement.webkitRequestFullscreen ||
                documentBody.msRequestFullscreen)) {
            fullScreenButton.classList.remove('noVNC_hidden');
            this.addFullscreenHandlers();
        }
    }

    initSettings(): void {
        let i;

        // Logging selection dropdown
        let llevels = ['error', 'warn', 'info', 'debug'];
        for (i = 0; i < llevels.length; i += 1) {
            this.addOption(document.getElementById('noVNC_setting_logging'), llevels[i], llevels[i]);
        }

        // Settings with immediate effects
        this.initSetting('logging', 'warn');
        this.updateLogging();

        // if port == 80 (or 443) then it won't be present and should be
        // set manually
        // let port: any = window.location.port;
        if (!this.port) {
            if (window.location.protocol.substring(0, 5) == 'https') {
                this.port = 443;
            } else if (window.location.protocol.substring(0, 4) == 'http') {
                this.port = 80;
            }
        }

        /* Populate the controls if defaults are provided in the URL */
        this.initSetting('host', this.host);
        this.initSetting('port', this.port);
        this.initSetting('encrypt', (window.location.protocol === 'https:'));
        this.initSetting('cursor', !isTouchDevice);
        this.initSetting('clip', false);
        this.initSetting('resize', 'off');
        this.initSetting('shared', true);
        this.initSetting('view_only', this.viewOnly);
        this.initSetting('path', 'websockify');
        this.initSetting('repeaterID', '');
        this.initSetting('reconnect', false);
        this.initSetting('reconnect_delay', 5000);

        this.setupSettingLabels();
    }
    // Adds a link to the label elements on the corresponding input elements
    setupSettingLabels(): void {
        let labels = document.getElementsByTagName('LABEL') as NodeListOf<HTMLLabelElement>;
        if (labels) {
            for (let i = 0; i < labels.length; i++) {
                let label: HTMLLabelElement = labels[i];
                let htmlFor = label.htmlFor;
                if (htmlFor != '') {
                    let elem = document.getElementById(htmlFor) as any;
                    if (elem) { elem.label = label; }
                } else {
                    // If 'for' isn't set, use the first input element child
                    let children = label.children as HTMLCollection;
                    for (let j = 0; j < children.length; j++) {
                        let child = children[j] as any;
                        if (child.form !== undefined) {
                            child.label = labels[i];
                            break;
                        }
                    }
                }
            }
        }
    }

    initRFB(): boolean {
        try {
            this.rfb = new RFB({
                'target': document.getElementById('vnc_canvas'),
                'onNotification': this.notification.bind(this),
                'onUpdateState': this.updateState.bind(this),
                'onDisconnected': this.disconnectFinished.bind(this),
                'onPasswordRequired': this.passwordRequired.bind(this),
                'onXvpInit': this.updateXvpButton.bind(this),
                'onClipboard': this.clipboardReceive.bind(this),
                'onFBUComplete': this.initialResize.bind(this),
                'onFBResize': this.updateSessionSize.bind(this),
                'onDesktopName': this.updateDesktopName.bind(this),
            });
            return true;
        } catch (exc) {
            let msg = 'Unable to create RFB client -- ' + exc;
            Log.Error(msg);
            this._statusObs.next('Error: ' + msg);
            return false;
        }
    }

    /* ------^-------
    *     /INIT
    * ==============
    * EVENT HANDLERS
    * ------v------*/

    addResizeHandlers() {
        window.onresize = function () {
            if (this.resizeTimeout) {
                clearTimeout(this.resizeTimeout);
            }
            this.resizeTimeout = setTimeout((() => {
                this.applyResizeMode();
                this.updateViewClip();
            }).bind(this), 500);
        }.bind(this);
        // window.addEventListener('resize', this.applyResizeMode);
        // window.addEventListener('resize', this.updateViewClip);
    }

    addControlbarHandlers() {
        let controlBar = document.getElementById('noVNC_control_bar');
        if (controlBar !== undefined && controlBar !== null) {
            controlBar.addEventListener('mousemove', this.activateControlbar);
            controlBar.addEventListener('mouseup', this.activateControlbar);
            controlBar.addEventListener('mousedown', this.activateControlbar);
            controlBar.addEventListener('keypress', this.activateControlbar);
            controlBar.addEventListener('mousedown', this.keepControlbar);
            controlBar.addEventListener('keypress', this.keepControlbar);
        }

        let viewDragButton = document.getElementById('noVNC_view_drag_button');
        if (viewDragButton !== undefined && viewDragButton !== null) {
            viewDragButton.addEventListener('click', this.toggleViewDrag);
        }

        let controlBarHandle = document.getElementById('noVNC_control_bar_handle');
        if (controlBarHandle !== undefined && controlBarHandle !== null) {
            controlBarHandle.addEventListener('mousedown', this.controlbarHandleMouseDown);
            controlBarHandle.addEventListener('mouseup', this.controlbarHandleMouseUp);
            controlBarHandle.addEventListener('mousemove', this.dragControlbarHandle);
        }
        // resize events aren't available for elements
        window.addEventListener('resize', this.updateControlbarHandle);

        let exps = document.getElementsByClassName('noVNC_expander');
        for (let i = 0; i < exps.length; i++) {
            exps[i].addEventListener('click', this.toggleExpander);
        }
    }

    setMouseButtonListener(element, mouseBtn) {
        if (element !== undefined && element !== null) {
            element.addEventListener('click', function () { this.setMouseButton(mouseBtn); });
        }
    }

    addTouchSpecificHandlers() {
        this.setMouseButtonListener(document.getElementById('noVNC_mouse_button0'), 1);
        this.setMouseButtonListener(document.getElementById('noVNC_mouse_button1'), 2);
        this.setMouseButtonListener(document.getElementById('noVNC_mouse_button2'), 4);
        this.setMouseButtonListener(document.getElementById('noVNC_mouse_button4'), 0);

        if (document.getElementById('noVNC_keyboard_button') !== undefined && document.getElementById('noVNC_keyboard_button') !== null) {
            document.getElementById('noVNC_keyboard_button')
                .addEventListener('click', this.toggleVirtualKeyboard);
        }

        let keyboardInput = document.getElementById('noVNC_keyboardinput');
        if (keyboardInput !== undefined && keyboardInput !== null) {
            keyboardInput.addEventListener('input', this.keyInput);
            keyboardInput.addEventListener('focus', this.onfocusVirtualKeyboard);
            keyboardInput.addEventListener('blur', this.onblurVirtualKeyboard);
            keyboardInput.addEventListener('submit', function () { return false; });
        }

        document.documentElement
            .addEventListener('mousedown', this.keepVirtualKeyboard, true);


        let controlBar = document.getElementById('noVNC_control_bar');
        if (controlBar !== undefined && controlBar !== null) {
            controlBar.addEventListener('touchstart', this.activateControlbar);
            controlBar.addEventListener('touchmove', this.activateControlbar);
            controlBar.addEventListener('touchend', this.activateControlbar);
            controlBar.addEventListener('input', this.activateControlbar);

            controlBar.addEventListener('touchstart', this.keepControlbar);
            controlBar.addEventListener('input', this.keepControlbar);
        }

        let controlBarHandle = document.getElementById('noVNC_control_bar_handle');
        if (controlBarHandle !== undefined && controlBarHandle !== null) {
            controlBarHandle.addEventListener('touchstart', this.controlbarHandleMouseDown);
            controlBarHandle.addEventListener('touchend', this.controlbarHandleMouseUp);
            controlBarHandle.addEventListener('touchmove', this.dragControlbarHandle);
        }
    }

    addExtraKeysHandlers() {
        if (document.getElementById('noVNC_toggle_extra_keys_button')) {
            document.getElementById('noVNC_toggle_extra_keys_button')
                .addEventListener('click', this.toggleExtraKeys);
        }
        if (document.getElementById('noVNC_toggle_ctrl_button')) {
            document.getElementById('noVNC_toggle_ctrl_button')
                .addEventListener('click', this.toggleCtrl);
        }
        if (document.getElementById('noVNC_toggle_alt_button')) {
            document.getElementById('noVNC_toggle_alt_button')
                .addEventListener('click', this.toggleAlt);
        }
        if (document.getElementById('noVNC_send_tab_button')) {
            document.getElementById('noVNC_send_tab_button')
                .addEventListener('click', this.sendTab);
        }
        if (document.getElementById('noVNC_send_esc_button')) {
            document.getElementById('noVNC_send_esc_button')
                .addEventListener('click', this.sendEsc);
        }
        if (document.getElementById('noVNC_send_ctrl_alt_del_button')) {
            document.getElementById('noVNC_send_ctrl_alt_del_button')
                .addEventListener('click', this.sendCtrlAltDel);
        }
    }

    addXvpHandlers() {
        let _this = this;
        if (document.getElementById('noVNC_xvp_shutdown_button')) {
            document.getElementById('noVNC_xvp_shutdown_button')
                .addEventListener('click', function () { _this.rfb.xvpShutdown(); });
        }
        if (document.getElementById('noVNC_xvp_reboot_button')) {
            document.getElementById('noVNC_xvp_reboot_button')
                .addEventListener('click', function () { _this.rfb.xvpReboot(); });
        }
        if (document.getElementById('noVNC_xvp_reset_button')) {
            document.getElementById('noVNC_xvp_reset_button')
                .addEventListener('click', function () { _this.rfb.xvpReset(); });
        }
        if (document.getElementById('noVNC_xvp_button')) {
            document.getElementById('noVNC_xvp_button')
                .addEventListener('click', _this.toggleXvpPanel);
        }
    }

    addConnectionControlHandlers() {
        if (document.getElementById('noVNC_disconnect_button')) {
            document.getElementById('noVNC_disconnect_button')
                .addEventListener('click', this.disconnect);
        }
        if (document.getElementById('noVNC_connect_button')) {
            document.getElementById('noVNC_connect_button')
                .addEventListener('click', this.connect);
        }
        if (document.getElementById('noVNC_cancel_reconnect_button')) {
            document.getElementById('noVNC_cancel_reconnect_button')
                .addEventListener('click', this.cancelReconnect);
        }
        if (document.getElementById('noVNC_password_button')) {
            document.getElementById('noVNC_password_button')
                .addEventListener('click', this.setPassword);
        }
    }

    addClipboardHandlers() {
        if (document.getElementById('noVNC_clipboard_button')) {
            document.getElementById('noVNC_clipboard_button')
                .addEventListener('click', this.toggleClipboardPanel);
        }
        if (document.getElementById('noVNC_clipboard_text')) {
            document.getElementById('noVNC_clipboard_text')
                .addEventListener('focus', this.displayBlur);

            document.getElementById('noVNC_clipboard_text')
                .addEventListener('blur', this.displayFocus);

            document.getElementById('noVNC_clipboard_text')
                .addEventListener('change', this.clipboardSend);
        }
        if (document.getElementById('noVNC_clipboard_clear_button')) {
            document.getElementById('noVNC_clipboard_clear_button')
                .addEventListener('click', this.clipboardClear);
        }
    }

    // Add a call to save settings when the element changes,
    // unless the optional parameter changeFunc is used instead.
    addSettingChangeHandler(name, changeFunc?) {
        if (document.getElementById('noVNC_setting_' + name)) {
            let settingElem = document.getElementById('noVNC_setting_' + name);
            if (changeFunc === undefined) {
                changeFunc = function () { this.saveSetting(name); };
            }
            settingElem.addEventListener('change', changeFunc);
        }
    }

    addSettingsHandlers() {
        if (document.getElementById('noVNC_settings_button')) {
            document.getElementById('noVNC_settings_button')
                .addEventListener('click', this.toggleSettingsPanel);
        }
        this.addSettingChangeHandler('encrypt');
        this.addSettingChangeHandler('cursor');
        this.addSettingChangeHandler('cursor', this.updateLocalCursor);
        this.addSettingChangeHandler('resize');
        this.addSettingChangeHandler('resize', this.enableDisableViewClip);
        this.addSettingChangeHandler('resize', this.applyResizeMode);
        this.addSettingChangeHandler('clip');
        this.addSettingChangeHandler('clip', this.updateViewClip);
        this.addSettingChangeHandler('shared');
        this.addSettingChangeHandler('view_only');
        this.addSettingChangeHandler('view_only', this.updateViewOnly);
        this.addSettingChangeHandler('host');
        this.addSettingChangeHandler('port');
        this.addSettingChangeHandler('path');
        this.addSettingChangeHandler('repeaterID');
        this.addSettingChangeHandler('logging');
        this.addSettingChangeHandler('logging', this.updateLogging);
        this.addSettingChangeHandler('reconnect');
        this.addSettingChangeHandler('reconnect_delay');
    }

    addFullscreenHandlers() {
        document.getElementById('noVNC_fullscreen_button')
            .addEventListener('click', this.toggleFullscreen);

        window.addEventListener('fullscreenchange', this.updateFullscreenButton);
        window.addEventListener('mozfullscreenchange', this.updateFullscreenButton);
        window.addEventListener('webkitfullscreenchange', this.updateFullscreenButton);
        window.addEventListener('msfullscreenchange', this.updateFullscreenButton);
    }

    /* ------^-------
     * /EVENT HANDLERS
     * ==============
     *     VISUAL
     * ------v------*/

    updateState(rfb, state, oldstate) {
        let msg: string;
        let statusInfo: string = '';

        switch (state) {
            case 'connecting':
                statusInfo = 'Connecting...';
                break;
            case 'connected':
                this.connected = true;
                this.inhibit_reconnect = false;
                statusInfo = 'Connected';
                if (rfb && rfb.get_encrypt()) {
                    msg = _('Connected (encrypted) to ') + this.desktopName;
                } else {
                    msg = _('Connected (unencrypted) to ') + this.desktopName;
                }
                break;
            case 'disconnecting':
                this.connected = false;
                statusInfo = 'Disconnecting...';
                break;
            case 'disconnected':
                statusInfo = 'Disconnected';
                break;
            default:
                msg = 'Invalid UI state';
                statusInfo = 'Error: ' + msg;
                Log.Error(msg);
                break;
        }
        this._statusObs.next(statusInfo);
        this.updateVisualState();
    }

    // Disable/enable controls depending on connection state
    updateVisualState() {
        this.enableDisableViewClip();

        if (cursorURIsSupported() && !isTouchDevice) {
            this.enableSetting('cursor');
        } else {
            this.disableSetting('cursor');
        }

        if (this.connected) {
            this.disableSetting('encrypt');
            this.disableSetting('shared');
            this.disableSetting('host');
            this.disableSetting('port');
            this.disableSetting('path');
            this.disableSetting('repeaterID');
            this.updateViewClip();
            this.setMouseButton(1);

            // Hide the controlbar after 2 seconds
            this.closeControlbarTimeout = setTimeout(this.closeControlbar.bind(this), 2000);
        } else {
            this.enableSetting('encrypt');
            this.enableSetting('shared');
            this.enableSetting('host');
            this.enableSetting('port');
            this.enableSetting('path');
            this.enableSetting('repeaterID');
            this.updateXvpButton(0);
            this.keepControlbar();
        }

        // Hide input related buttons in view only mode
        if (document.getElementById('noVNC_keyboard_button') && document.getElementById('noVNC_toggle_extra_keys_button')) {
            if (this.rfb && this.rfb.get_view_only()) {
                document.getElementById('noVNC_keyboard_button')
                    .classList.add('noVNC_hidden');
                document.getElementById('noVNC_toggle_extra_keys_button')
                    .classList.add('noVNC_hidden');
            } else {
                document.getElementById('noVNC_keyboard_button')
                    .classList.remove('noVNC_hidden');
                document.getElementById('noVNC_toggle_extra_keys_button')
                    .classList.remove('noVNC_hidden');
            }
        }

        // State change disables viewport dragging.
        // It is enabled (toggled) by direct click on the button
        this.setViewDrag(false);

        // State change also closes the password dialog
        if (document.getElementById('noVNC_password_dlg')) {
            document.getElementById('noVNC_password_dlg')
                .classList.remove('noVNC_open');
        }
    }

    notification(rfb, msg, level, options) {
        this._statusObs.next(msg + ', Level: ' + level);
    }

    activateControlbar(event?) {
        clearTimeout(this.idleControlbarTimeout);
        // We manipulate the anchor instead of the actual control
        // bar in order to avoid creating new a stacking group
        if (document.getElementById('noVNC_control_bar_anchor')) {
            document.getElementById('noVNC_control_bar_anchor')
                .classList.remove('noVNC_idle');
        }
        this.idleControlbarTimeout = window.setTimeout(this.idleControlbar, 2000);
    }

    idleControlbar() {
        document.getElementById('noVNC_control_bar_anchor')
            .classList.add('noVNC_idle');
    }

    keepControlbar() {
        clearTimeout(this.closeControlbarTimeout);
    }

    openControlbar() {
        if (document.getElementById('noVNC_control_bar')) {
            document.getElementById('noVNC_control_bar')
                .classList.add('noVNC_open');
        }
    }

    closeControlbar() {
        this.closeAllPanels();
        if (document.getElementById('noVNC_control_bar')) {
            document.getElementById('noVNC_control_bar')
                .classList.remove('noVNC_open');
        }
    }

    toggleControlbar() {
        if (document.getElementById('noVNC_control_bar')
            .classList.contains('noVNC_open')) {
            this.closeControlbar();
        } else {
            this.openControlbar();
        }
    }

    toggleControlbarSide() {
        // Temporarily disable animation to avoid weird movement
        let bar = document.getElementById('noVNC_control_bar');
        bar.style.transitionDuration = '0s';
        bar.addEventListener('transitionend', function () { this.style.transitionDuration = ''; });

        let anchor = document.getElementById('noVNC_control_bar_anchor');
        if (anchor.classList.contains('noVNC_right')) {
            WebUtil.writeSetting('controlbar_pos', 'left');
            anchor.classList.remove('noVNC_right');
        } else {
            WebUtil.writeSetting('controlbar_pos', 'right');
            anchor.classList.add('noVNC_right');
        }

        // Consider this a movement of the handle
        this.controlbarDrag = true;
    }

    showControlbarHint(show) {
        let hint = document.getElementById('noVNC_control_bar_hint');
        if (show) {
            hint.classList.add('noVNC_active');
        } else {
            hint.classList.remove('noVNC_active');
        }
    }

    dragControlbarHandle(e) {
        if (!this.controlbarGrabbed) return;

        let ptr = getPointerEvent(e);

        let anchor = document.getElementById('noVNC_control_bar_anchor');
        if (ptr.clientX < (window.innerWidth * 0.1)) {
            if (anchor.classList.contains('noVNC_right')) {
                this.toggleControlbarSide();
            }
        } else if (ptr.clientX > (window.innerWidth * 0.9)) {
            if (!anchor.classList.contains('noVNC_right')) {
                this.toggleControlbarSide();
            }
        }

        if (!this.controlbarDrag) {
            // The goal is to trigger on a certain physical width, the
            // devicePixelRatio brings us a bit closer but is not optimal.
            let dragThreshold = 10 * (window.devicePixelRatio || 1);
            let dragDistance = Math.abs(ptr.clientY - this.controlbarMouseDownClientY);

            if (dragDistance < dragThreshold) return;

            this.controlbarDrag = true;
        }

        let eventY = ptr.clientY - this.controlbarMouseDownOffsetY;

        this.moveControlbarHandle(eventY);

        e.preventDefault();
        e.stopPropagation();
        this.keepControlbar();
        this.activateControlbar();
    }

    // Move the handle but don't allow any position outside the bounds
    moveControlbarHandle(viewportRelativeY) {
        let handle = document.getElementById('noVNC_control_bar_handle');
        let handleHeight = handle.getBoundingClientRect().height;
        let controlbarBounds = document.getElementById('noVNC_control_bar')
            .getBoundingClientRect();
        let margin = 10;

        // These heights need to be non-zero for the below logic to work
        if (handleHeight === 0 || controlbarBounds.height === 0) {
            return;
        }

        let newY = viewportRelativeY;

        // Check if the coordinates are outside the control bar
        if (newY < controlbarBounds.top + margin) {
            // Force coordinates to be below the top of the control bar
            newY = controlbarBounds.top + margin;

        } else if (newY > controlbarBounds.top +
            controlbarBounds.height - handleHeight - margin) {
            // Force coordinates to be above the bottom of the control bar
            newY = controlbarBounds.top +
                controlbarBounds.height - handleHeight - margin;
        }

        // Corner case: control bar too small for stable position
        if (controlbarBounds.height < (handleHeight + margin * 2)) {
            newY = controlbarBounds.top +
                (controlbarBounds.height - handleHeight) / 2;
        }

        // The transform needs coordinates that are relative to the parent
        let parentRelativeY = newY - controlbarBounds.top;
        handle.style.transform = 'translateY(' + parentRelativeY + 'px)';
    }

    updateControlbarHandle() {
        // Since the control bar is fixed on the viewport and not the page,
        // the move function expects coordinates relative the the viewport.
        if (document.getElementById('noVNC_control_bar_handle')) {
            let handle = document.getElementById('noVNC_control_bar_handle');
            let handleBounds = handle.getBoundingClientRect();
            this.moveControlbarHandle(handleBounds.top);
        }
    }

    controlbarHandleMouseUp(e) {
        if ((e.type === 'mouseup') && (e.button !== 0)) { return; }

        // mouseup and mousedown on the same place toggles the controlbar
        if (this.controlbarGrabbed && !this.controlbarDrag) {
            this.toggleControlbar();
            e.preventDefault();
            e.stopPropagation();
            this.keepControlbar();
            this.activateControlbar();
        }
        this.controlbarGrabbed = false;
        this.showControlbarHint(false);
    }

    controlbarHandleMouseDown(e) {
        if ((e.type === 'mousedown') && (e.button !== 0)) { return; }

        let ptr = getPointerEvent(e);

        let handle = document.getElementById('noVNC_control_bar_handle');
        let bounds = handle.getBoundingClientRect();

        // Touch events have implicit capture
        if (e.type === 'mousedown') {
            setCapture(handle);
        }

        this.controlbarGrabbed = true;
        this.controlbarDrag = false;

        this.showControlbarHint(true);

        this.controlbarMouseDownClientY = ptr.clientY;
        this.controlbarMouseDownOffsetY = ptr.clientY - bounds.top;
        e.preventDefault();
        e.stopPropagation();
        this.keepControlbar();
        this.activateControlbar();
    }

    toggleExpander(e) {
        // if (this.classList.contains('noVNC_open')) {
        //     this.classList.remove('noVNC_open');
        // } else {
        //     this.classList.add('noVNC_open');
        // }
    }

    /* ------^-------
     *    /VISUAL
     * ==============
     *    SETTINGS
     * ------v------*/

    // Initial page load read/initialization of settings
    initSetting(name, defVal) {
        // Check Query string followed by cookie
        let val = WebUtil.getConfigVar(name);
        if (val === null || val === undefined) {
            val = WebUtil.readSetting(name, defVal);
        }
        this.updateSetting(name, val);
        return val;
    }

    // Update cookie and form control setting. If value is not set, then
    // updates from control to current cookie setting.
    updateSetting(name, value?) {

        // Save the cookie for this session
        if (typeof value !== 'undefined') {
            WebUtil.writeSetting(name, value);
        }

        // Update the settings control
        value = this.getSetting(name);
        let ctrl;

        if (name === 'resize') {
            ctrl = document.getElementById('noVNC_setting_' + name) as HTMLInputElement;
        }
        else {
            ctrl = document.getElementById('noVNC_setting_' + name) as HTMLSelectElement;
        }
        // let ctrl = document.getElementById('noVNC_setting_' + name) as HTMLInputElement;
        if (ctrl === null || ctrl === undefined) { return; }
        if (ctrl.type === 'checkbox') {
            ctrl.checked = value;
        } else if (typeof ctrl.options !== 'undefined') {
            for (let i = 0; i < ctrl.options.length; i += 1) {
                if (ctrl.options[i].value === value) {
                    ctrl.selectedIndex = i;
                    break;
                }
            }
        } else {
            /*Weird IE9 error leads to 'null' appearring
            in textboxes instead of ''.*/
            if (value === null) {
                value = '';
            }
            ctrl.value = value;
        }
    }

    // Save control setting to cookie
    saveSetting(name) {
        let val, ctrl;
        if (name === 'resize') {
            ctrl = document.getElementById('noVNC_setting_' + name) as HTMLInputElement;
        }
        else {
            ctrl = document.getElementById('noVNC_setting_' + name) as HTMLSelectElement;
        }
        if (ctrl === null || ctrl === undefined) { return; }
        if (ctrl.type === 'checkbox') {
            val = ctrl.checked;
        } else if (typeof ctrl.options !== 'undefined') {
            val = ctrl.options[ctrl.selectedIndex].value;
        } else {
            val = ctrl.value;
        }
        WebUtil.writeSetting(name, val);
        return val;
    }

    // Read form control compatible setting from cookie
    getSetting(name) {
        let ctrl = document.getElementById('noVNC_setting_' + name) as any;
        let val = WebUtil.readSetting(name);
        if (ctrl !== undefined && ctrl !== null
            && typeof val !== 'undefined' && val !== null && ctrl.type === 'checkbox') {
            if (val.toString().toLowerCase() in { '0': 1, 'no': 1, 'false': 1 }) {
                val = false;
            } else {
                val = true;
            }
        }
        return val;
    }

    // These helpers compensate for the lack of parent-selectors and
    // previous-sibling-selectors in CSS which are needed when we want to
    // disable the labels that belong to disabled input elements.
    disableSetting(name) {
        let ctrl = document.getElementById('noVNC_setting_' + name) as any;
        if (ctrl) {
            ctrl.disabled = true;
            ctrl.label.classList.add('noVNC_disabled');
        }
    }

    enableSetting(name) {
        let ctrl = document.getElementById('noVNC_setting_' + name) as any;
        if (ctrl) {
            ctrl.disabled = false;
            ctrl.label.classList.remove('noVNC_disabled');
        }
    }

    /* ------^-------
     *   /SETTINGS
     * ==============
     *    PANELS
     * ------v------*/

    closeAllPanels() {
        this.closeSettingsPanel();
        this.closeXvpPanel();
        this.closeClipboardPanel();
        this.closeExtraKeys();
    }

    /* ------^-------
     *   /PANELS
     * ==============
     * SETTINGS (panel)
     * ------v------*/

    openSettingsPanel() {
        this.closeAllPanels();
        this.openControlbar();

        // Refresh UI elements from saved cookies
        this.updateSetting('encrypt');
        if (cursorURIsSupported()) {
            this.updateSetting('cursor');
        } else {
            this.updateSetting('cursor', !isTouchDevice);
            this.disableSetting('cursor');
        }
        this.updateSetting('clip');
        this.updateSetting('resize');
        this.updateSetting('shared');
        this.updateSetting('view_only');
        this.updateSetting('path');
        this.updateSetting('repeaterID');
        this.updateSetting('logging');
        this.updateSetting('reconnect');
        this.updateSetting('reconnect_delay');

        if (document.getElementById('noVNC_settings')) {
            document.getElementById('noVNC_settings')
                .classList.add('noVNC_open');
        }
        if (document.getElementById('noVNC_settings_button')) {
            document.getElementById('noVNC_settings_button')
                .classList.add('noVNC_selected');
        }
    }

    closeSettingsPanel() {
        if (document.getElementById('noVNC_settings')) {
            document.getElementById('noVNC_settings')
                .classList.remove('noVNC_open');
        }
        if (document.getElementById('noVNC_settings_button')) {
            document.getElementById('noVNC_settings_button')
                .classList.remove('noVNC_selected');
        }
    }

    toggleSettingsPanel() {
        if (document.getElementById('noVNC_settings')
            .classList.contains('noVNC_open')) {
            this.closeSettingsPanel();
        } else {
            this.openSettingsPanel();
        }
    }

    /* ------^-------
     *   /SETTINGS
     * ==============
     *      XVP
     * ------v------*/

    openXvpPanel() {
        this.closeAllPanels();
        this.openControlbar();

        if (document.getElementById('noVNC_xvp')) {
            document.getElementById('noVNC_xvp')
                .classList.add('noVNC_open');
        }
        if (document.getElementById('noVNC_xvp_button')) {
            document.getElementById('noVNC_xvp_button')
                .classList.add('noVNC_selected');
        }
    }

    closeXvpPanel() {
        if (document.getElementById('noVNC_xvp')) {
            document.getElementById('noVNC_xvp')
                .classList.remove('noVNC_open');
        }
        if (document.getElementById('noVNC_xvp_button')) {
            document.getElementById('noVNC_xvp_button')
                .classList.remove('noVNC_selected');
        }
    }

    toggleXvpPanel() {
        if (document.getElementById('noVNC_xvp')
            .classList.contains('noVNC_open')) {
            this.closeXvpPanel();
        } else {
            this.openXvpPanel();
        }
    }

    // Disable/enable XVP button
    updateXvpButton(ver) {
        let xvpButton = document.getElementById('noVNC_xvp_button');
        if (xvpButton === null || xvpButton === undefined) { return; }
        if (ver >= 1 && !this.rfb.get_view_only()) {
            xvpButton.classList.remove('noVNC_hidden');
        } else {
            xvpButton.classList.add('noVNC_hidden');
            // Close XVP panel if open
            this.closeXvpPanel();
        }
    }

    /* ------^-------
     *     /XVP
     * ==============
     *   CLIPBOARD
     * ------v------*/

    openClipboardPanel() {
        this.closeAllPanels();
        this.openControlbar();

        if (document.getElementById('noVNC_clipboard')) {
            document.getElementById('noVNC_clipboard')
                .classList.add('noVNC_open');
        }
        if (document.getElementById('noVNC_clipboard_button')) {
            document.getElementById('noVNC_clipboard_button')
                .classList.add('noVNC_selected');
        }
    }

    closeClipboardPanel() {
        if (document.getElementById('noVNC_clipboard')) {
            document.getElementById('noVNC_clipboard')
                .classList.remove('noVNC_open');
        }
        if (document.getElementById('noVNC_clipboard_button')) {
            document.getElementById('noVNC_clipboard_button')
                .classList.remove('noVNC_selected');
        }
    }

    toggleClipboardPanel() {
        if (document.getElementById('noVNC_clipboard')
            .classList.contains('noVNC_open')) {
            this.closeClipboardPanel();
        } else {
            this.openClipboardPanel();
        }
    }

    clipboardReceive(rfb, text) {
        let clipboardText = document.getElementById('noVNC_clipboard_text') as HTMLTextAreaElement;
        if (clipboardText) {
            clipboardText.value = text;
        }
    }

    clipboardClear() {
        let clipboardText = document.getElementById('noVNC_clipboard_text') as HTMLTextAreaElement;
        if (clipboardText) {
            clipboardText.value = '';
        }
        this.rfb.clipboardPasteFrom('');
    }

    clipboardSend() {
        let clipboardText = document.getElementById('noVNC_clipboard_text') as HTMLTextAreaElement;
        if (clipboardText) {
            this.rfb.clipboardPasteFrom(clipboardText);
        }
    }

    /* ------^-------
     *  /CLIPBOARD
     * ==============
     *  CONNECTION
     * ------v------*/

    openConnectPanel() {
        if (document.getElementById('noVNC_connect_dlg')) {
            document.getElementById('noVNC_connect_dlg')
                .classList.add('noVNC_open');
        }
    }

    closeConnectPanel() {
        if (document.getElementById('noVNC_connect_dlg')) {
            document.getElementById('noVNC_connect_dlg')
                .classList.remove('noVNC_open');
        }
    }

    connect(event?, password = this.password) {
        let path = this.getSetting('path');

        if (password === undefined) {
            password = WebUtil.getConfigVar('password');
        }

        if (password === null) {
            password = undefined;
        }

        if ((!this.host) || (!this.port)) {
            let msg: string = _('Must set host and port');
            Log.Error(msg);
            this._statusObs.next('Error: ' + msg);
            return;
        }

        if (!this.initRFB()) { return; }

        this.closeAllPanels();
        this.closeConnectPanel();

        this.rfb.set_encrypt(this.getSetting('encrypt'));
        this.rfb.set_shared(this.getSetting('shared'));
        this.rfb.set_repeaterID(this.getSetting('repeaterID'));

        this.updateLocalCursor();
        this.updateViewOnly();
        this.rfb.connect(this.host, this.port, this.password, path);
    }

    disconnect() {
        this.closeAllPanels();
        this.rfb.disconnect();

        // Disable automatic reconnecting
        this.inhibit_reconnect = true;

        // Restore the callback used for initial resize
        this.rfb.set_onFBUComplete(this.initialResize);

        // Don't display the connection settings until we're actually disconnected
    }

    reconnect() {
        this.reconnect_callback = undefined;

        // if reconnect has been disabled in the meantime, do nothing.
        if (this.inhibit_reconnect) {
            return;
        }

        this.connect(null, this.reconnect_password);
    }

    disconnectFinished(rfb, reason) {
        if (typeof reason !== 'undefined') {
            this._statusObs.next('Error: ' + reason);
        } else if (this.getSetting('reconnect') === true && !this.inhibit_reconnect) {
            document.getElementById('noVNC_transition_text').textContent = _('Reconnecting...');
            document.documentElement.classList.add('noVNC_reconnecting');

            let delay = parseInt(this.getSetting('reconnect_delay'));
            this.reconnect_callback = setTimeout(this.reconnect, delay);
            return;
        }

        this.openControlbar();
        this.openConnectPanel();
    }

    cancelReconnect() {
        if (this.reconnect_callback !== null) {
            clearTimeout(this.reconnect_callback);
            this.reconnect_callback = undefined;
        }

        document.documentElement.classList.remove('noVNC_reconnecting');
        this.openControlbar();
        this.openConnectPanel();
    }

    /* ------^-------
     *  /CONNECTION
     * ==============
     *   PASSWORD
     * ------v------*/

    passwordRequired(rfb, msg) {
        let psw_dlg = document.getElementById('noVNC_password_dlg');
        if (psw_dlg && psw_dlg.classList !== undefined) {
            psw_dlg.classList.add('noVNC_open');
        }

        let psw_input = document.getElementById('noVNC_password_input');
        if (psw_input) {
            setTimeout(() => {
                psw_input.focus();
            }, 100);
        }

        if (typeof msg === 'undefined') {
            msg = _('Password is required');
        }
        Log.Warn(msg);
        this._statusObs.next('Warning: ' + msg);
    }

    setPassword(e) {
        let inputElem: HTMLInputElement = document.getElementById('noVNC_password_input') as HTMLInputElement;
        let password: string;
        if (inputElem) {
            password = inputElem.value;
            // Clear the input after reading the password
            inputElem.value = '';
        } else {
            password = this.password;
        }
        if (password) {
            this.rfb.sendPassword(password);
            this.reconnect_password = password;
        }
        let psw_dlg = document.getElementById('noVNC_password_dlg');
        if (psw_dlg && psw_dlg.classList !== undefined) {
            psw_dlg.classList.remove('noVNC_open');
        }
        // Prevent actually submitting the form
        e.preventDefault();
    }

    /* ------^-------
     *  /PASSWORD
     * ==============
     *   FULLSCREEN
     * ------v------*/

    toggleFullscreen() {
        let documentVar = document as any;
        let documentElement = document.documentElement as any;
        let documentBody = document.body as any;

        if (documentVar.fullscreenElement || // alternative standard method
            documentVar.mozFullScreenElement || // currently working methods
            documentVar.webkitFullscreenElement ||
            documentVar.msFullscreenElement) {
            if (documentVar.exitFullscreen) {
                documentVar.exitFullscreen();
            } else if (documentVar.mozCancelFullScreen) {
                documentVar.mozCancelFullScreen();
            } else if (documentVar.webkitExitFullscreen) {
                documentVar.webkitExitFullscreen();
            } else if (documentVar.msExitFullscreen) {
                documentVar.msExitFullscreen();
            }
        } else {
            let element: any = Element as any;
            if (documentElement.requestFullscreen) {
                documentElement.requestFullscreen();
            } else if (documentElement.mozRequestFullScreen) {
                documentElement.mozRequestFullScreen();
            } else if (documentElement.webkitRequestFullscreen) {
                documentElement.webkitRequestFullscreen(element.ALLOW_KEYBOARD_INPUT);
            } else if (documentBody.msRequestFullscreen) {
                documentBody.msRequestFullscreen();
            }
        }
        this.enableDisableViewClip();
        this.updateFullscreenButton();
    }

    updateFullscreenButton() {
        let documentVar: any = document as any;

        if (documentVar.fullscreenElement || // alternative standard method
            documentVar.mozFullScreenElement || // currently working methods
            documentVar.webkitFullscreenElement ||
            documentVar.msFullscreenElement) {
            documentVar.getElementById('noVNC_fullscreen_button')
                .classList.add('noVNC_selected');
        } else {
            documentVar.getElementById('noVNC_fullscreen_button')
                .classList.remove('noVNC_selected');
        }
    }

    /* ------^-------
     *  /FULLSCREEN
     * ==============
     *     RESIZE
     * ------v------*/

    // Apply remote resizing or local scaling
    applyResizeMode() {
        if (!this.rfb) { return; }

        let screen = this.screenSize();

        if (screen && this.connected && this.rfb.get_display()) {

            let display = this.rfb.get_display();

            display.set_scale(1);

            // Make sure the viewport is adjusted first
            this.updateViewClip();

            if (this.resizeMode === 'remote') {

                // Request changing the resolution of the remote display to
                // the size of the local browser viewport.

                // In order to not send multiple requests before the browser-resize
                // is finished we wait 0.5 seconds before sending the request.
                clearTimeout(this.resizeTimeout);
                this.resizeTimeout = setTimeout(function () {
                    // Request a remote size covering the viewport
                    this.rfb.requestDesktopSize(screen.w, screen.h)
                }, 500);

            } else if (this.resizeMode === 'scale' || this.resizeMode === 'downscale') {

                let downscaleOnly = this.resizeMode === 'downscale';
                display.autoscale(screen.w, screen.h, downscaleOnly);
                this.fixScrollbars();
            }
        }
    }

    // Gets the the size of the available viewport in the browser window
    screenSize() {
        let screen = document.getElementById('vnc_screen');
        return { w: screen.offsetWidth, h: screen.offsetHeight };
    }

    // Normally we only apply the current resize mode after a window resize
    // event. This means that when a new connection is opened, there is no
    // resize mode active.
    // We have to wait until the first FBU because this is where the client
    // will find the supported encodings of the server. Some calls later in
    // the chain is dependant on knowing the server-capabilities.
    initialResize(rfb, fbu) {
        this.applyResizeMode();
        // After doing this once, we remove the callback.
        this.rfb.set_onFBUComplete(function () { });
    }

    /* ------^-------
     *    /RESIZE
     * ==============
     *    CLIPPING
     * ------v------*/

    // Set and configure viewport clipping
    setViewClip(clip) {
        this.updateSetting('clip', clip);
        this.updateViewClip();
    }

    // Update parameters that depend on the clip setting
    updateViewClip() {
        if (!this.rfb) { return; }

        let display = this.rfb.get_display();
        let cur_clip = display.get_viewport();
        let new_clip = this.getSetting('clip');

        if (this.resizeMode === 'downscale' || this.resizeMode === 'scale') {
            // Disable clipping if we are scaling
            new_clip = false;
        } else if (isTouchDevice) {
            // Touch devices usually have shit scrollbars
            new_clip = true;
        }

        if (cur_clip !== new_clip) {
            display.set_viewport(new_clip);
        }

        let size = this.screenSize();

        if (new_clip && size) {
            // When clipping is enabled, the screen is limited to
            // the size of the browser window.
            display.viewportChangeSize(size.w, size.h);
            this.fixScrollbars();
        }

        // Changing the viewport may change the state of
        // the dragging button
        this.updateViewDrag();
    }

    // Handle special cases where clipping is forced on/off or locked
    enableDisableViewClip() {
        // Disable clipping if we are scaling, connected or on touch
        if (this.resizeMode === 'downscale' || this.resizeMode === 'scale' ||
            isTouchDevice) {
            this.disableSetting('clip');
        } else {
            this.enableSetting('clip');
        }
    }

    /* ------^-------
     *   /CLIPPING
     * ==============
     *    VIEWDRAG
     * ------v------*/

    toggleViewDrag() {
        if (!this.rfb) { return; }

        let drag = this.rfb.get_viewportDrag();
        this.setViewDrag(!drag);
    }

    // Set the view drag mode which moves the viewport on mouse drags
    setViewDrag(drag) {
        if (!this.rfb) { return; }

        this.rfb.set_viewportDrag(drag);

        this.updateViewDrag();
    }

    updateViewDrag() {
        let clipping = false;

        if (!this.connected) { return; }

        // Check if viewport drag is possible. It is only possible
        // if the remote display is clipping the client display.
        if (this.rfb.get_display().get_viewport() &&
            this.rfb.get_display().clippingDisplay()) {
            clipping = true;
        }

        let viewDragButton = document.getElementById('noVNC_view_drag_button') as HTMLInputElement;
        if (viewDragButton) {

            if (!clipping &&
                this.rfb.get_viewportDrag()) {
                // The size of the remote display is the same or smaller
                // than the client display. Make sure viewport drag isn't
                // active when it can't be used.
                this.rfb.set_viewportDrag(false);
            }

            if (this.rfb.get_viewportDrag()) {
                viewDragButton.classList.add('noVNC_selected');
            } else {
                viewDragButton.classList.remove('noVNC_selected');
            }

            // Different behaviour for touch vs non-touch
            // The button is disabled instead of hidden on touch devices
            if (isTouchDevice) {
                viewDragButton.classList.remove('noVNC_hidden');

                if (clipping) {
                    viewDragButton.disabled = false;
                } else {
                    viewDragButton.disabled = true;
                }
            } else {
                viewDragButton.disabled = false;

                if (clipping) {
                    viewDragButton.classList.remove('noVNC_hidden');
                } else {
                    viewDragButton.classList.add('noVNC_hidden');
                }
            }
        }
    }

    /* ------^-------
     *   /VIEWDRAG
     * ==============
     *    KEYBOARD
     * ------v------*/

    showVirtualKeyboard() {
        if (!isTouchDevice) return;

        let input = document.getElementById('noVNC_keyboardinput') as HTMLTextAreaElement;

        if (document.activeElement == input) return;

        input.focus();

        try {
            let l = input.value.length;
            // Move the caret to the end
            input.setSelectionRange(l, l);
        } catch (err) { } // setSelectionRange is undefined in Google Chrome
    }

    hideVirtualKeyboard() {
        if (!isTouchDevice) { return; }

        let input = document.getElementById('noVNC_keyboardinput');

        if (document.activeElement != input) { return; }

        input.blur();
    }

    toggleVirtualKeyboard() {
        if (document.getElementById('noVNC_keyboard_button')
            .classList.contains('noVNC_selected')) {
            this.hideVirtualKeyboard();
        } else {
            this.showVirtualKeyboard();
        }
    }

    onfocusVirtualKeyboard(event) {
        document.getElementById('noVNC_keyboard_button')
            .classList.add('noVNC_selected');
    }

    onblurVirtualKeyboard(event) {
        document.getElementById('noVNC_keyboard_button')
            .classList.remove('noVNC_selected');
    }

    keepVirtualKeyboard(event) {
        let input = document.getElementById('noVNC_keyboardinput');

        // Only prevent focus change if the virtual keyboard is active
        if (document.activeElement != input) {
            return;
        }

        // Only allow focus to move to other elements that need
        // focus to function properly
        if (event.target.form !== undefined) {
            switch (event.target.type) {
                case 'text':
                case 'email':
                case 'search':
                case 'password':
                case 'tel':
                case 'url':
                case 'textarea':
                case 'select-one':
                case 'select-multiple':
                    return;
                default:
                    return;
            }
        }

        event.preventDefault();
    }

    keyboardinputReset() {
        let kbi = document.getElementById('noVNC_keyboardinput') as HTMLTextAreaElement;
        if (kbi) {
            kbi.value = new Array(this.defaultKeyboardinputLen).join('_');
            this.lastKeyboardinput = kbi.value;
        }
    }

    // When normal keyboard events are left uncought, use the input events from
    // the keyboardinput element instead and generate the corresponding key events.
    // This code is required since some browsers on Android are inconsistent in
    // sending keyCodes in the normal keyboard events when using on screen keyboards.
    keyInput(event) {

        if (!this.rfb) { return; }

        let newValue = event.target.value;

        if (!this.lastKeyboardinput) {
            this.keyboardinputReset();
        }
        let oldValue = this.lastKeyboardinput;

        let newLen;
        try {
            // Try to check caret position since whitespace at the end
            // will not be considered by value.length in some browsers
            newLen = Math.max(event.target.selectionStart, newValue.length);
        } catch (err) {
            // selectionStart is undefined in Google Chrome
            newLen = newValue.length;
        }
        let oldLen = oldValue.length;

        let backspaces;
        let inputs = newLen - oldLen;
        if (inputs < 0) {
            backspaces = -inputs;
        } else {
            backspaces = 0;
        }

        // Compare the old string with the new to account for
        // text-corrections or other input that modify existing text
        let i;
        for (i = 0; i < Math.min(oldLen, newLen); i++) {
            if (newValue.charAt(i) != oldValue.charAt(i)) {
                inputs = newLen - i;
                backspaces = oldLen - i;
                break;
            }
        }

        // Send the key events
        for (i = 0; i < backspaces; i++) {
            this.rfb.sendKey(KeyTable.XK_BackSpace, 'Backspace');
        }
        for (i = newLen - inputs; i < newLen; i++) {
            this.rfb.sendKey(keysyms.lookup(newValue.charCodeAt(i)));
        }

        // Control the text content length in the keyboardinput element
        if (newLen > 2 * this.defaultKeyboardinputLen) {
            this.keyboardinputReset();
        } else if (newLen < 1) {
            // There always have to be some text in the keyboardinput
            // element with which backspace can interact.
            this.keyboardinputReset();
            // This sometimes causes the keyboard to disappear for a second
            // but it is required for the android keyboard to recognize that
            // text has been added to the field
            event.target.blur();
            // This has to be ran outside of the input handler in order to work
            setTimeout(event.target.focus.bind(event.target), 0);
        } else {
            this.lastKeyboardinput = newValue;
        }
    }

    /* ------^-------
     *   /KEYBOARD
     * ==============
     *   EXTRA KEYS
     * ------v------*/

    openExtraKeys() {
        this.closeAllPanels();
        this.openControlbar();

        if (document.getElementById('noVNC_modifiers')) {
            document.getElementById('noVNC_modifiers')
                .classList.add('noVNC_open');
        }
        if (document.getElementById('noVNC_toggle_extra_keys_button')) {
            document.getElementById('noVNC_toggle_extra_keys_button')
                .classList.add('noVNC_selected');
        }
    }

    closeExtraKeys() {
        if (document.getElementById('noVNC_modifiers')) {
            document.getElementById('noVNC_modifiers')
                .classList.remove('noVNC_open');
        }
        if (document.getElementById('noVNC_toggle_extra_keys_button')) {
            document.getElementById('noVNC_toggle_extra_keys_button')
                .classList.remove('noVNC_selected');
        }
    }

    toggleExtraKeys() {
        if (document.getElementById('noVNC_modifiers')
            .classList.contains('noVNC_open')) {
            this.closeExtraKeys();
        } else {
            this.openExtraKeys();
        }
    }

    sendEsc() {
        this.rfb.sendKey(KeyTable.XK_Escape, 'Escape');
    }

    sendTab() {
        this.rfb.sendKey(KeyTable.XK_Tab);
    }

    toggleCtrl() {
        let btn = document.getElementById('noVNC_toggle_ctrl_button');
        if (btn) {
            if (btn.classList.contains('noVNC_selected')) {
                this.rfb.sendKey(KeyTable.XK_Control_L, 'ControlLeft', false);
                btn.classList.remove('noVNC_selected');
            } else {
                this.rfb.sendKey(KeyTable.XK_Control_L, 'ControlLeft', true);
                btn.classList.add('noVNC_selected');
            }
        }
    }

    toggleAlt() {
        let btn = document.getElementById('noVNC_toggle_alt_button');
        if (btn) {
            if (btn.classList.contains('noVNC_selected')) {
                this.rfb.sendKey(KeyTable.XK_Alt_L, 'AltLeft', false);
                btn.classList.remove('noVNC_selected');
            } else {
                this.rfb.sendKey(KeyTable.XK_Alt_L, 'AltLeft', true);
                btn.classList.add('noVNC_selected');
            }
        }
    }

    sendCtrlAltDel() {
        this.rfb.sendCtrlAltDel();
    }

    /* ------^-------
     *   /EXTRA KEYS
     * ==============
     *     MISC
     * ------v------*/

    setMouseButton(num) {
        let view_only = this.rfb.get_view_only();
        if (this.rfb && !view_only) {
            this.rfb.get_mouse().set_touchButton(num);
        }

        let blist = [0, 1, 2, 4];
        for (let b = 0; b < blist.length; b++) {
            let button = document.getElementById('noVNC_mouse_button' +
                blist[b]);
            if (button) {
                if (blist[b] === num && !view_only) {
                    button.classList.remove('noVNC_hidden');
                } else {
                    button.classList.add('noVNC_hidden');
                }
            }
        }
    }

    displayBlur() {
        if (this.rfb && !this.rfb.get_view_only()) {
            this.rfb.get_keyboard().set_focused(false);
            this.rfb.get_mouse().set_focused(false);
        }
    }

    displayFocus() {
        if (this.rfb && !this.rfb.get_view_only()) {
            this.rfb.get_keyboard().set_focused(true);
            this.rfb.get_mouse().set_focused(true);
        }
    }

    updateLocalCursor() {
        if (!this.rfb) { return; }
        this.rfb.set_local_cursor(this.getSetting('cursor'));
    }

    updateViewOnly() {
        if (!this.rfb) { return; }
        this.rfb.set_view_only(this.viewOnly);
    }

    updateLogging() {
        WebUtil.init_logging(this.getSetting('logging'));
    }

    updateSessionSize(rfb, width, height) {
        this.updateViewClip();
        this.fixScrollbars();
    }

    fixScrollbars() {
        // This is a hack because Chrome screws up the calculation
        // for when scrollbars are needed. So to fix it we temporarily
        // toggle them off and on.
        let screen = document.getElementById('vnc_screen');
        screen.style.overflow = 'hidden';
        // Force Chrome to recalculate the layout by asking for
        // an element's dimensions
        screen.getBoundingClientRect();
        screen.style.overflow = '';
    }

    updateDesktopName(rfb, name) {
        this.desktopName = name;
        // Display the desktop name in the document title
        // document.title = name + ' - noVNC';
    }

    // Helper to add options to dropdown.
    addOption(selectbox, text, value) {
        if (selectbox !== undefined && selectbox !== null) {
            let optn = document.createElement('OPTION') as HTMLOptionElement;
            optn.text = text;
            optn.value = value;
            selectbox.options.add(optn);
        }
    }

    /* ------^-------
     *    /MISC
     * ==============
     */


    init() {
        // Set up translations
        let LINGUAS = ['de', 'el', 'nl', 'sv'];
        l10n.setup(LINGUAS);
        if (l10n.language !== 'en' && l10n.dictionary === undefined) {
            WebUtil.fetchJSON('app/locale/' + l10n.language + '.json', function (translations) {
                l10n.dictionary = translations;

                // wait for translations to load before loading the UI
                this.prime();
            }, function (err) {
                throw err;
            });
        } else {
            this.prime();
        }
    }
}
