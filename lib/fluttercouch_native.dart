import 'dart:async';
import 'package:flutter/services.dart';
import 'package:fluttercouch/fluttercouch.dart';

class FluttercouchNative extends Fluttercouch {

  ///////////////////////////////////////////////////////////////////////
  ///
  /// Use case-specific methods to customize this plugin
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

  Future<List<dynamic>> getEntriesForLabel(int rowsPerPage, String startDate, String labelId, bool isFuture, String cutoffDate) async {
    try {
      // String cutoffDate = DateTime.now().toIso8601String();
      final List<dynamic> result = await Fluttercouch.methodChannel.invokeMethod(
          'getEntriesForLabel', <String, dynamic>{
        'rowsPerPage': rowsPerPage,
        'startDate': startDate, // for rows batch / pagination
        'labelId': labelId,
        'isFuture': isFuture, // vs. past 
        'cutoffDate':cutoffDate, // for future/past
      });
      return result;
    } on PlatformException {
      throw 'unable to get entries for label';
    }
  }

  Future<List<dynamic>> getEntriesForPaper( /* int rowsPerPage, */ String paperId) async {
    try {
      final List<dynamic> result = await Fluttercouch.methodChannel.invokeMethod(
          'getEntriesForPaper', <String, dynamic>{
        'paperId': paperId
      });
      return result;
    } on PlatformException {
      throw 'unable to get entries for paper';
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
