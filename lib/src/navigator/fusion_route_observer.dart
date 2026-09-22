import 'package:flutter/material.dart';

class FusionRouteObserverManager {
  FusionRouteObserverManager._();
  static final _instance = FusionRouteObserverManager._();
  static FusionRouteObserverManager get instance => _instance;
  List<FusionRouteObserver>? routeObservers;
  void didRemove(Route route) {
    final copyRouteObservers = List<FusionRouteObserver>.from(routeObservers ?? []);
    for (final element in copyRouteObservers) {
      element.didRemove(route);
    }
  }
  void didAdd(Route route) {
    final copyRouteObservers = List<FusionRouteObserver>.from(routeObservers ?? []);
    for (final element in copyRouteObservers) {
      element.didAdd(route);
    }
  }

  /// remove no use observer
  void removeRouteObserver(FusionRouteObserver routeObserver) {
    routeObservers?.remove(routeObserver);
  }

  /// add observer
  void registerRouteObserver(FusionRouteObserver routeObserver) {
    routeObservers?.add(routeObserver);
  }
}

abstract interface class FusionRouteObserver {
  void didRemove(Route route);
  void didAdd(Route route);
}