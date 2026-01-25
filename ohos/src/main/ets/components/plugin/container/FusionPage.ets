import FusionConstant from '../constant/FusionConstant'
import FusionComponent from './FusionComponent'
import FusionEntry from './FusionEntry'

@Entry({ routeName: FusionPage.ROUTE_NAME })
@Component
export default struct FusionPage {
  static ROUTE_NAME = 'FusionPage'
  param?: Record<string, Object>
  private entry?: FusionEntry

  override aboutToAppear(): void {
    if (!this.param) {
      this.param = LocalStorage.getShared().get('params') as Record<string, Object> | undefined
    }
    this.entry = new FusionEntry(
      getContext(this),
      this.param
    )
  }

  override build(): void {
    NavDestination() {
      FusionComponent({ entry: this.entry })
    }
    .mode(this.isTransparent() ? NavDestinationMode.DIALOG : NavDestinationMode.STANDARD)
    .hideTitleBar(true)
    .onBackPressed(() => {
      this.entry?.onBackPress()
      return true
    })
  }

  private isTransparent(): boolean {
    return (this.param![FusionConstant.EXTRA_BACKGROUND_MODE] ?? false) as boolean
  }

  // @Entry
  override onPageShow() {
    this.entry?.onPageShow()
  }

  // @Entry
  override onPageHide() {
    this.entry?.onPageHide()
  }

  // @Entry
  override onBackPress(): boolean {
    this.entry?.onBackPress()
    return true
  }
}