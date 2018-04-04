import { Component, Input, OnInit, OnChanges } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { BreadcrumbService } from './breadcrumb.service';

/**
 * This component shows a breadcrumb trail for available routes the router can navigate to.
 * It subscribes to the router in order to update the breadcrumb trail as you navigate to a component.
 */
@Component({
  selector: 'breadcrumb',
  templateUrl: './breadcrumb.component.html',
  styleUrls: ['./breadcrumb.component.scss'],
})
export class BreadcrumbComponent implements OnInit, OnChanges {
  @Input() useBootstrap: boolean = true;
  @Input() prefix: string = '';

  public _urls: string[];
  public _routerSubscription: any;

  constructor(private router: Router, private breadcrumbService: BreadcrumbService) {}

  ngOnInit(): void {
    this._urls = new Array();

    if (this.prefix.length > 0) {
      this._urls.unshift(this.prefix);
    }
    if (this.router.navigated) {
      this._urls.length = 0; //Fastest way to clear out array
      this.generateBreadcrumbTrail(this.router.routerState.snapshot.url);
      this._routerSubscription = this.router.events.subscribe((navigationEnd: NavigationEnd) => {
        if (navigationEnd instanceof NavigationEnd) {
          this._urls.length = 0; //Fastest way to clear out array
          this.generateBreadcrumbTrail(navigationEnd.urlAfterRedirects ? navigationEnd.urlAfterRedirects : navigationEnd.url);
        }
      });
    }
  }

  ngOnChanges(changes: any): void {
    if (!this._urls) {
      return;
    }

    this._urls.length = 0;
    this.generateBreadcrumbTrail(this.router.url);
  }

  generateBreadcrumbTrail(url: string): void {
    if (!this.breadcrumbService.isRouteHidden(url)) {
      //Add url to beginning of array (since the url is being recursively broken down from full url to its parent)
      this._urls.unshift(url);
    }

    if (url.lastIndexOf('/') > 0) {
      this.generateBreadcrumbTrail(url.substr(0, url.lastIndexOf('/'))); //Find last '/' and add everything before it as a parent route
    } else if (this.prefix.length > 0) {
      this._urls.unshift(this.prefix);
    }
  }

  navigateTo(url: string): void {
    this.router.navigateByUrl(url);
  }

  friendlyName(url: string): string {
    let itemsToChange: number = 0;

    itemsToChange = this.getChangesToBeResponsive();
    if (this._urls.indexOf(url) <= itemsToChange - 1) {
      return this._urls.indexOf(url) === 0 ? '...' : '/ ...';
    } else {
      return !url ? '' : this.breadcrumbService.getFriendlyNameForRoute(url);
    }
  }

  private getLengthOfBreadcrumb(): number {
    let length: number = 0;
    let namesToChange: number = 0;
    this._urls.forEach((route: string) => {
      length += this.breadcrumbService.getFriendlyNameForRoute(route).length;
    });

    return length;
  }

  private getChangesToBeResponsive(): number {
    let itemsToChange: number = 0;
    let width: number = window.innerWidth;
    let length: number = this.getLengthOfBreadcrumb();

    if (width > 860 && width < 960 && length > 50) {
      itemsToChange = this.wordsNumberToChange(length, 50);
    } else if (width >= 960 && width < 1070 && length > 65) {
      itemsToChange = this.wordsNumberToChange(length, 65);
    } else if (width >= 1070 && width < 1140 && length > 80) {
      itemsToChange = this.wordsNumberToChange(length, 80);
    } else if (width >= 1140 && width < 1250 && length > 90) {
      itemsToChange = this.wordsNumberToChange(length, 90);
    } else if (width >= 1250 && length > 100) {
      itemsToChange = this.wordsNumberToChange(length, 100);
    }
    return itemsToChange;
  }

  private wordsNumberToChange(totalLength: number, limit: number): number {
    let wordsNumber: number = 0;
    let newTotalLength: number = totalLength;
    for (let url of this._urls) {
      newTotalLength = newTotalLength - this.breadcrumbService.getFriendlyNameForRoute(url).length + '/ ...'.length;

      if (newTotalLength <= limit) {
        wordsNumber++;
        return wordsNumber;
      }
      wordsNumber++;
    }
    return 0;
  }

  /*ngOnDestroy(): void {
    this._routerSubscription.unsubscribe();
  }*/
}
