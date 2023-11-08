import {
  Directive,
  Input,
  TemplateRef,
  ViewContainerRef
} from "@angular/core";
import { FeatureFlagsService } from "../../services/feature-toggle.service";

@Directive({
  selector: "[featureFlag]",
})
export class FeatureFlagDirective {
  constructor(
    private tpl: TemplateRef<any>,
    private vcr: ViewContainerRef,
    private featureFlagService: FeatureFlagsService
  ) { }


  @Input() set featureFlag(featureName: string) {
    const isEnabled = this.featureFlagService.isFeatureEnabled(featureName);
    if (isEnabled) {
      this.vcr.createEmbeddedView(this.tpl);
    } else {
      this.vcr.clear();
    }
  }
}

