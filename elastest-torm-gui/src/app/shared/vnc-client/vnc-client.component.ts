import { AfterViewInit, Component, ElementRef, HostListener, Input, OnInit, ViewChild } from '@angular/core';
import * as WebUtil from '../../../assets/vnc-resources/app/webutil';
import RFB from '../../../assets/vnc-resources/core/rfb';
import * as UI from '../../../assets/vnc-resources/app/ui';

@Component({
  selector: 'vnc-client',
  templateUrl: './vnc-client.component.html',
  styleUrls: ['./vnc-client.component.scss']
})
export class VncClientComponent implements AfterViewInit, OnInit {
  //http://localhost:4200/#/vnc?host=localhost&port=6080&password=secret
  @Input()
  public host: string;
  @Input()
  public port: string;
  @Input()
  public password: string;

  public canvas: HTMLCanvasElement;

  public path;
  public rfb;
  public resizeTimeout;
  public desktopName;

  constructor(private elementRef: ElementRef) {
    this.canvas = <HTMLCanvasElement>document.getElementById('vnc_canvas');
  }

  @HostListener('window:resize')
  onWindowResize() {
    //debounce resize, wait for resize to finish before doing stuff
    if (this.resizeTimeout) {
      clearTimeout(this.resizeTimeout);
    }
    this.resizeTimeout = setTimeout((() => {
      this.uiResize();
    }).bind(this), 500);
  }

  ngOnInit() {
    // this.createScript('./assets/vnc-resources/vendor/promise.js');
    // this.createScript('./assets/vnc-resources/vendor/browser-es-module-loader/dist/browser-es-module-loader.js');
  }

  ngAfterViewInit(): void {
    //Use ui.js to load:
    UI.default;

    //Use this component to load:
    // if (this.host === undefined || this.host === null) {
    //   this.host = 'localhost';
    // }
    // if (this.port === undefined || this.port === null) {
    //   this.port = '6080';
    // }
    // WebUtil.init_logging(WebUtil.getConfigVar('logging', 'warn'));

    // this.path = WebUtil.getConfigVar('path', 'websockify');

    // // If a token variable is passed in, set the parameter in a cookie.
    // // This is used by nova-novncproxy.
    // let token = WebUtil.getConfigVar('token', null);
    // if (token) {
    //   // if token is already present in the path we should use it
    //   this.path = WebUtil.injectParamIfMissing(this.path, 'token', token);

    //   WebUtil.createCookie('token', token, 1)
    // }

    // this.connect();
  }

  createScript(path: string, type: string = 'text/javascript') {
    // document.write('<script type="' + type + '" src="' + path + '"></script>');
    const s = document.createElement('script');
    s.type = 'text/javascript';
  
    s.src = path;
    this.elementRef.nativeElement.appendChild(s);
  }

  uiResize() {
    if (WebUtil.getConfigVar('resize', false)) {
      let innerW: number = window.innerWidth;
      let innerH: number = window.innerHeight;
      if (innerW !== undefined && innerH !== undefined) {
        this.rfb.requestDesktopSize(innerW, innerH);
      }
    }
  }

  FBUComplete(rfb, fbu) {
    // this.uiResize();
    rfb.set_onFBUComplete(function () { });
  }

  updateDesktopName(rfb, name) {
    this.desktopName = name;
  }

  passwordRequired(rfb, msg) {
    if (typeof msg === 'undefined') {
      msg = 'Password Required: ';
    }
    let html;

    let form = document.createElement('form');
    form.innerHTML = '<label></label>';
    form.innerHTML += '<input type=password size=10 id="password_input" class="noVNC_status">';
    form.onsubmit = this.setPassword;

    // bypass status() because it sets text content
    document.getElementById('noVNC_status_bar').setAttribute('class', 'noVNC_status_warn');
    document.getElementById('noVNC_status').innerHTML = '';
    document.getElementById('noVNC_status').appendChild(form);
    document.getElementById('noVNC_status').querySelector('label').textContent = msg;
  }

  setPassword() {
    (this.rfb.sendPassword(document.getElementById('password_input') as any).value);
    return false;
  }

  sendCtrlAltDel() {
    this.rfb.sendCtrlAltDel();
    return false;
  }

  xvpShutdown() {
    this.rfb.xvpShutdown();
    return false;
  }

  xvpReboot() {
    this.rfb.xvpReboot();
    return false;
  }

  xvpReset() {
    this.rfb.xvpReset();
    return false;
  }

  status(text, level) {
    switch (level) {
      case 'normal':
      case 'warn':
      case 'error':
        break;
      default:
        level = 'warn';
    }
  }

  updateState(rfb, state, oldstate) {
    if (this.canvas === undefined) {
      this.canvas = <HTMLCanvasElement>document.getElementById('vnc_canvas');
    }
    if (this.canvas !== undefined) {
      let width: number = this.canvas.width + 30;
      let height: number = this.canvas.height + 105;
      window.resizeTo(width, height);
    }
    // let cad = document.getElementById('sendCtrlAltDelButton');
    // switch (state) {
    //   case 'connecting':
    //     this.status("Connecting", "normal");
    //     break;
    //   case 'connected':
    //     if (rfb && rfb.get_encrypt()) {
    //       this.status("Connected (encrypted) to " +
    //         this.desktopName, "normal");
    //     } else {
    //       this.status("Connected (unencrypted) to " +
    //         this.desktopName, "normal");
    //     }
    //     break;
    //   case 'disconnecting':
    //     this.status("Disconnecting", "normal");
    //     break;
    //   case 'disconnected':
    //     this.status("Disconnected", "normal");
    //     break;
    //   default:
    //     this.status(state, "warn");
    //     break;
    // }

    // if (state === 'connected') {
    //   cad.disabled = false;
    // } else {
    //   cad.disabled = true;
    //   this.xvpInit(0);
    // }
  }

  disconnected(rfb, reason) {
    if (typeof (reason) !== 'undefined') {
      console.error(reason, 'error');
    }
  }

  notification(rfb, msg, level, options) {
    console.log(msg, level);
  }



  xvpInit(ver) {
    let xvpbuttons;
    xvpbuttons = document.getElementById('noVNC_xvp_buttons');
    if (ver >= 1) {
      xvpbuttons.style.display = 'inline';
    } else {
      xvpbuttons.style.display = 'none';
    }
  }

  // document.getElementById('sendCtrlAltDelButton').style.display = "inline";
  // document.getElementById('sendCtrlAltDelButton').onclick = sendCtrlAltDel;
  // document.getElementById('xvpShutdownButton').onclick = xvpShutdown;
  // document.getElementById('xvpRebootButton').onclick = xvpReboot;
  // document.getElementById('xvpResetButton').onclick = xvpReset;


  connect() {
    if ((!this.host) || (!this.port)) {
      console.log('Must specify host and port in URL', 'error');
    }

    try {
      this.rfb = new RFB({
        'target': document.getElementById('vnc_canvas'),
        'encrypt': WebUtil.getConfigVar('encrypt',
          (window.location.protocol === 'https:')),
        'repeaterID': WebUtil.getConfigVar('repeaterID', ''),
        'local_cursor': WebUtil.getConfigVar('cursor', true),
        'shared': WebUtil.getConfigVar('shared', true),
        'view_only': WebUtil.getConfigVar('view_only', false),
        'onNotification': this.notification,
        'onUpdateState': this.updateState,
        'onDisconnected': this.disconnected,
        // 'onXvpInit': this.xvpInit,
        'onPasswordRequired': this.passwordRequired,
        'onFBUComplete': this.FBUComplete,
        'onDesktopName': this.updateDesktopName,
      });
    } catch (exc) {
      console.log('Unable to create RFB client -- ' + exc, 'error');
      return; // don't continue trying to connect
    }

    this.rfb.connect(this.host, this.port, this.password, this.path);
  }
}
