import 'dart:async';

import 'package:flutter/services.dart';
import 'package:fluttercouch/document.dart';

abstract class FluttercouchCore {
  static const MethodChannel methodChannel =
      const MethodChannel('it.oltrenuovefrontiere.fluttercouch');

  static const EventChannel _replicationEventChannel = const EventChannel(
      "it.oltrenuovefrontiere.fluttercouch/replicationEventChannel");

  Future<String> initDatabaseWithName(String _name) async {
    try {
      final String result =
          await methodChannel.invokeMethod('initDatabaseWithName', _name);
      return result;
    } on PlatformException catch (e) {
      throw 'unable to init database $_name: ${e.message}';
    }
  }

  Future<bool> initView(String view) async {
    try {
      final bool result = await methodChannel.invokeMethod('initView', view);
      return result;
    } on PlatformException {
      throw 'unable to init view:' + view;
    }
  }

  // returns {"_id": _id, "_rev": _rev}
  Future<Map<dynamic, dynamic>> saveDocument(Document _doc) async {
    try {
      final Map<dynamic, dynamic> result =
          await methodChannel.invokeMethod('saveDocument', _doc.toMap());
      return result;
    } on PlatformException {
      throw 'unable to save the document';
    }
  }

  // returns {"_id": _id, "_rev": _rev}
  Future<Map<dynamic, dynamic>> saveDocumentWithId(
      String _id, Document _doc) async {
    try {
      final Map<dynamic, dynamic> result = await methodChannel.invokeMethod(
          'saveDocumentWithId',
          <String, dynamic>{'id': _id, 'map': _doc.toMap()});
      return result;
    } on PlatformException {
      throw 'unable to save the document with set id $_id';
    }
  }

  Future<Document> getDocumentWithId(String _id) async {
    Map<dynamic, dynamic> _docResult;
    _docResult = await _getDocumentWithId(_id);
    return Document(_docResult["doc"], _docResult["id"]);
  }

  // returns {"_id": _id, "_rev": _rev}
  Future<Map<dynamic, dynamic>> updateDocumentWithId(
      String _id, Document _doc) async {
    final Map<dynamic, dynamic> updated = await methodChannel.invokeMethod(
        'updateDocumentWithId',
        <String, dynamic>{'id': _id, 'map': _doc.toMap()});
    return updated;
  }

  Future<bool> deleteDocumentWithId(String _id) async {
    bool result = await _deleteDocumentWithId(_id);
    return result;
  }

  Future<Null> createReplicatorWithName(String _name) async {
    try {
      await methodChannel.invokeMethod('createReplicatorWithName', _name);
    } on PlatformException {
      throw 'unable to create replicator';
    }
  }

  Future<String> configureReplicator(Map<String, String> _config) async {
    try {
      final String result =
          await methodChannel.invokeMethod('configureReplicator', _config);
      return result;
    } on PlatformException {
      throw 'unable to initialize replicator with config ${_config.toString()}';
    }
  }

  Future<Null> startReplicator() async {
    try {
      await methodChannel.invokeMethod('startReplicator');
    } on PlatformException {
      throw 'unable to start replication';
    }
  }

  Future<Null> stopReplicator() async {
    try {
      await methodChannel.invokeMethod('stopReplicator');
    } on PlatformException {
      throw 'unable to stop replication';
    }
  }

  Future<Map<dynamic, dynamic>> _getDocumentWithId(String _id) async {
    try {
      final Map<dynamic, dynamic> result =
          await methodChannel.invokeMethod('getDocumentWithId', _id);
      return result;
    } on PlatformException {
      throw 'unable to get the document with id $_id';
    }
  }

  Future<bool> _deleteDocumentWithId(String _id) async {
    try {
      final bool result =
          await methodChannel.invokeMethod('deleteDocumentWithId', _id);
      return result;
    } on PlatformException {
      throw 'unable to delete the document with id $_id';
    }
  }

  // returns {"_id": _id, "_rev": _rev, "attacName": attachName}
  Future<Map<dynamic, dynamic>> upsertNamedAttachmentAsFilepath(String _id,  Map<String, String> _map) async {
    final Map<dynamic, dynamic> upserted = await methodChannel.invokeMethod(
        'upsertNamedAttachmentAsFilepath', <String, dynamic>{'id': _id, 'map': _map});
    return upserted;
  }

  // // returns {"_id": _id, "stream": inputStream}
  // Future<Map<dynamic, dynamic>> getNamedAttachment(String _id, String _name) async {
  //   final Map<dynamic, dynamic> attachment = await methodChannel.invokeMethod(
  //       'getNamedAttachment', <String, dynamic>{'id': _id, 'name': _name});
  //   return attachment;
  // }

  void listenReplicationEvents(Function(dynamic) function) {
    _replicationEventChannel.receiveBroadcastStream().listen(function);
  }
  
}
