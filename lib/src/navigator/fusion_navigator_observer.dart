import 'package:flutter/material.dart';
import '../container/fusion_overlay.dart';
import 'fusion_route_observer.dart';

class FusionNavigatorObserver extends NavigatorObserver {
  @override
  void didPush(Route route, Route? previousRoute) {
    FusionNavigatorObserverManager.instance.navigatorObservers
        ?.forEach((observer) {
      observer.didPush(route, previousRoute);
    });
  }

  @override
  void didPop(Route route, Route? previousRoute) {
    final uniqueId =
        FusionOverlayManager.instance.findContainerByRoute(route)?.uniqueId;
    bool result = FusionOverlayManager.instance.containerRoutesMap[uniqueId]
            ?.remove(route) ??
        false;
    if (result) {
      FusionRouteObserverManager.instance.didRemove(route);
    }
    FusionNavigatorObserverManager.instance.navigatorObservers
        ?.forEach((observer) {
      observer.didPop(route, previousRoute);
    });
  }

  @override
  void didRemove(Route route, Route? previousRoute) {
    final uniqueId =
        FusionOverlayManager.instance.findContainerByRoute(route)?.uniqueId;
    final result = FusionOverlayManager.instance.containerRoutesMap[uniqueId]
            ?.remove(route) ??
        false;
    if (result) {
      FusionRouteObserverManager.instance.didRemove(route);
    }
    FusionNavigatorObserverManager.instance.navigatorObservers
        ?.forEach((observer) {
      observer.didRemove(route, previousRoute);
    });
  }

  @override
  void didReplace({Route? newRoute, Route? oldRoute}) {
    FusionNavigatorObserverManager.instance.navigatorObservers
        ?.forEach((observer) {
      observer.didReplace(newRoute: newRoute, oldRoute: oldRoute);
    });
  }
}

/// showDialog & showModalBottomSheet等 useRootNavigator: true
class FusionRootNavigatorObserver extends NavigatorObserver {
  @override
  void didPush(Route route, Route? previousRoute) {
    if (route is PageRoute) {
      return;
    }
    FusionOverlayManager.instance.rootRoutes.add(route);
    FusionRouteObserverManager.instance.didAdd(route);
  }

  @override
  void didPop(Route route, Route? previousRoute) {
    final result = FusionOverlayManager.instance.rootRoutes.remove(route);
    if (result) {
      FusionRouteObserverManager.instance.didRemove(route);
    }
  }

  @override
  void didRemove(Route route, Route? previousRoute) {
    final result = FusionOverlayManager.instance.rootRoutes.remove(route);
    if (result) {
      FusionRouteObserverManager.instance.didRemove(route);
    }
  }
}

class FusionNavigatorObserverManager {
  FusionNavigatorObserverManager._();
  static final _instance = FusionNavigatorObserverManager._();
  static FusionNavigatorObserverManager get instance => _instance;
  List<NavigatorObserver>? navigatorObservers;
}
