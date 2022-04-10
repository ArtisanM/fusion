import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fusion/fusion.dart';
import 'package:fusion/log/fusion_log.dart';

class TestPage extends StatefulWidget {
  TestPage({Key? key, this.arguments}) : super(key: key) {
    _channel = const MethodChannel('fusion');
  }

  final Map<String, dynamic>? arguments;

  late final MethodChannel _channel;

  @override
  State<TestPage> createState() => _TestPageState();
}

class _TestPageState extends State<TestPage> {
  int _count = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.arguments?['title'] ?? '未知页面',
            style: AppBarTheme.of(context).titleTextStyle),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            InkWell(
              child: const Text('push /normal'),
              onTap: () {
                FusionNavigator.instance
                    .push('/normal', arguments: {'title': '12121'});
              },
            ),
            const SizedBox(
              height: 20,
            ),
            InkWell(
                child: const Text('push /test'),
                onTap: () async {
                  final result = await FusionNavigator.instance
                      .push<String?>('/test', arguments: {'title': '2'});
                  FusionLog.log('result=$result');
                }),
            const SizedBox(
              height: 20,
            ),
            InkWell(
                child: const Text('push /lifecycle'),
                onTap: () async {
                  final result = await FusionNavigator.instance.push<String?>(
                      '/lifecycle',
                      arguments: {'title': 'Lifecycle Test'});
                  FusionLog.log('result=$result');
                }),
            const SizedBox(
              height: 20,
            ),
            InkWell(
                child: const Text('pop'),
                onTap: () {
                  FusionNavigator.instance.pop('我是返回结果');
                }),
            const SizedBox(
              height: 20,
            ),
            InkWell(
                child: const Text('plugin'),
                onTap: () async {
                  final result =
                      await widget._channel.invokeMethod('getPlatformVersion');
                  FusionLog.log('result=$result');
                }),
            Text('$_count')
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          FusionLog.log('${ModalRoute.of(context).hashCode}');
          // setState(() {
          //   ++_count;
          // });
        },
        child: const Icon(Icons.add),
      ),
    );
  }
}
