import 'dart:async';

import 'package:flutter/services.dart';
import 'package:fluttercouch/document.dart';

abstract class Fluttercouch {
  static const MethodChannel _methodChannel =
      const MethodChannel('it.oltrenuovefrontiere.fluttercouch');

  static const EventChannel _replicationEventChannel =
    const EventChannel("it.oltrenuovefrontiere.fluttercouch/replicationEventChannel");

  Future<String> initDatabaseWithName(String _name) async {
    try {
      final String result =
          await _methodChannel.invokeMethod('initDatabaseWithName', _name);
      return result;
    } on PlatformException catch (e) {
      throw 'unable to init database $_name: ${e.message}';
    }
  }

  Future<bool> initView(String view) async {
     try {
      final bool result =
      await _methodChannel.invokeMethod('initView', view);
      return result;
    } on PlatformException {
      throw 'unable to init view:' + view;
    }
  }

  // returns {"_id": _id, "_rev": _rev}
  Future<Map<dynamic,dynamic>> saveDocument(Document _doc) async {
    try {
      final Map<dynamic,dynamic> result = await _methodChannel.invokeMethod('saveDocument', _doc.toMap());
      return result;
    } on PlatformException {
      throw 'unable to save the document';
    }
  }

  // returns {"_id": _id, "_rev": _rev}
  Future<Map<dynamic,dynamic>> saveDocumentWithId(String _id, Document _doc) async {
    try {
      final Map<dynamic,dynamic> result = await _methodChannel.invokeMethod(
          'saveDocumentWithId', <String, dynamic>{'id': _id, 'map': _doc.toMap()});
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
  Future<Map<dynamic,dynamic>> updateDocumentWithId(String _id, Document _doc) async {
      final Map<dynamic,dynamic> updated =
      await _methodChannel.invokeMethod('updateDocumentWithId', <String, dynamic>{'id': _id, 'map': _doc.toMap()});
      return updated;
  }

  Future<bool> deleteDocumentWithId(String _id) async {
    bool result = await _deleteDocumentWithId(_id);
    return result;
  }

  Future<String> setReplicatorEndpoint(String _endpoint) async {
    try {
      final String result =
          await _methodChannel.invokeMethod('setReplicatorEndpoint', _endpoint);
      return result;
    } on PlatformException {
      throw 'unable to set target endpoint to $_endpoint';
    }
  }

  Future<String> setReplicatorType(String _type) async {
    try {
      final String result =
          await _methodChannel.invokeMethod('setReplicatorType', _type);
      return result;
    } on PlatformException {
      throw 'unable to set replicator type to $_type';
    }
  }

  Future<bool> setReplicatorContinuous(bool _continuous) async {
    try {
      final bool result = await _methodChannel.invokeMethod('setReplicatorContinuous', _continuous);
      return result;
    } on PlatformException {
      throw 'unable to set replicator continuous setting to $_continuous';
    }
  }

  Future<String> setReplicatorBasicAuthentication(
      Map<String, String> _auth) async {
    try {
      final String result = await _methodChannel.invokeMethod(
          'setReplicatorBasicAuthentication', _auth);
      return result;
    } on PlatformException {
      throw 'unable to set replicator basic authentication';
    }
  }

  Future<String> setReplicatorSessionAuthentication(String _sessionID) async {
    try {
      final String result = await _methodChannel.invokeMethod('setReplicatorSessionAuthentication', _sessionID);
      return result;
    } on PlatformException {
      throw 'unable to set replicator basic authentication';
    }
  }

  Future<Null> initReplicator() async {
    try {
      await _methodChannel.invokeMethod("initReplicator");
    } on PlatformException {
      throw 'unable to init replicator';
    }
  }

  Future<Null> startReplicator() async {
    try {
      await _methodChannel.invokeMethod('startReplicator');
    } on PlatformException {
      throw 'unable to start replication';
    }
  }

  Future<Null> stopReplicator() async {
    try {
      await _methodChannel.invokeMethod('stopReplicator');
    } on PlatformException {
      throw 'unable to stop replication';
    }
  }

  Future<Map<dynamic, dynamic>> _getDocumentWithId(String _id) async {
    try {
      final Map<dynamic, dynamic> result =
      await _methodChannel.invokeMethod('getDocumentWithId', _id);
      return result;
    } on PlatformException {
      throw 'unable to get the document with id $_id';
    }
  }

  Future<bool> _deleteDocumentWithId(String _id) async {
    try {
      final bool result =
      await _methodChannel.invokeMethod('deleteDocumentWithId', _id);
      return result;
    } on PlatformException {
      throw 'unable to delete the document with id $_id';
    }
  }

  void listenReplicationEvents(Function(dynamic) function) {
    _replicationEventChannel.receiveBroadcastStream().listen(function);
  }

  ///////////////////////////////////////////////////////////////////////
  ///
  /// App-specific methods to customize this plugin
  ///
  //////////////////////////////////////////////////////////////////////
  
    Future<List<dynamic>> getAllEntries() async {
     try {
      final List<dynamic> result =
      await _methodChannel.invokeMethod('getAllEntries');
      return result;
    } on PlatformException {
      throw 'unable to get all entries';
    }
  }
  
}
