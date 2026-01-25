import { Fusion, FusionComponent, FusionEntry } from 'fusion';
import NavigationFlutterPage from './NavigationFlutterPage';
import CustomFusionEntry from './CustomFusionEntry';
import { router } from '@kit.ArkUI';

@Entry
@Component
struct RouterFlutterPage {
  private entry?: FusionEntry

  override aboutToAppear(): void {
    this.entry = new CustomFusionEntry(
      getContext(this),
      router.getParams() as Record<string, Object>
    )
  }

  @Builder
  PageMap(name: string, param: Record<string, Object>) {
    if (name === 'NavigationFlutterPage') {
      NavigationFlutterPage({ param })
    }
  }

  override build(): void {
    Navigation(Fusion.instance.navPathStack) {
      Stack() {
        FusionComponent({
          entry: this.entry
        })
      }
    }
    .mode(NavigationMode.Stack)
    .hideToolBar(true)
    .navDestination(this.PageMap)
  }

  onBackPress(): boolean | void {
    Fusion.instance.engineBinding?.maybePop()
    return true
  }
}