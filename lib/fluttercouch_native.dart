import 'dart:async';
import 'package:flutter/services.dart';
import 'package:fluttercouch/fluttercouch.dart';

// abstract class Fluttercouch extends Fluttercouch {
class FluttercouchNative extends Fluttercouch {
 

  ///////////////////////////////////////////////////////////////////////
  ///
  /// App-specific methods to customize this plugin
  ///
  //////////////////////////////////////////////////////////////////////

  Future<List<dynamic>> getPaperJournals() async {
    try {
      final List<dynamic> result =
          await Fluttercouch.methodChannel.invokeMethod('getPapers');
      return result;
    } on PlatformException {
      throw 'unable to get all papers';
    }
  }

  Future<List<dynamic>> getEntriesForLabel(int rowsPerPage, String startDate, String labelId) async {
    try {
      final List<dynamic> result = await Fluttercouch.methodChannel.invokeMethod(
          'getEntriesForLabel', <String, dynamic>{
        'rowsPerPage': rowsPerPage,
        'startDate': startDate,
        'labelId': labelId
      });
      return result;
    } on PlatformException {
      throw 'unable to get all entries';
    }
  }

  Future<List<dynamic>> getCategories() async {
    try {
      final List<dynamic> result =
          await Fluttercouch.methodChannel.invokeMethod('getCategories');
      return result;
    } on PlatformException {
      throw 'unable to get all categories';
    }
  }

  Future<List<dynamic>> getLabels(String categoryId) async {
    try {
      final List<dynamic> result = await Fluttercouch.methodChannel.invokeMethod(
          'getLabels', <String, dynamic>{'categoryId': categoryId});
      return result;
    } on PlatformException {
      throw 'unable to get labels for category: $categoryId';
    }
  }
}
