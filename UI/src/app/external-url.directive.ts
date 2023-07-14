import {
  Directive,
  HostBinding,
  PLATFORM_ID,
  Inject,
  Input
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Directive({
  selector: '[appExternalUrl]'
})
export class ExternalUrlDirective {
  @HostBinding('attr.rel') relAttr = '';
  @HostBinding('attr.target') targetAttr = '';
  @HostBinding('attr.href') hrefAttr = '';
  @Input() href: string;

  constructor(@Inject(PLATFORM_ID) private platformId: string) {}

  ngOnChanges() {
    this.hrefAttr = this.href;

    if (this.isLinkExternal()) {
      this.relAttr = '';
      this.targetAttr = '_blank';
    }
  }

  private isLinkExternal() {
    return (
      isPlatformBrowser(this.platformId) &&
      !this.href.includes(location.hostname)
    );
  }
}
