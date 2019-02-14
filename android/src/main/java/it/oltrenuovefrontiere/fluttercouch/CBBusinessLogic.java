package it.oltrenuovefrontiere.fluttercouch;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.View;
import com.couchbase.lite.Document;
import com.couchbase.lite.Revision;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.Attachment;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.Predicate;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.Document;
import com.couchbase.lite.support.LazyJsonArray;
// import com.couchbase.lite.android.AndroidContext;

import org.json.JSONObject;

import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.JSONMethodCodec;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import static android.content.ContentValues.TAG;

// TODO: copied all imports above; some not needed.

/**
 * CBBusinessLogic
 *
 * Implements delegated functionality from MethodCallHandler in CBManager.java.
*/
public class CBBusinessLogic {

    private static CBManager mCbManager;
    private static CBBusinessLogic mInstance;
   
    private CBBusinessLogic(CBManager manager) {
        mCbManager = manager;
    }

    public static CBBusinessLogic getInstance(CBManager manager) {
        if (mInstance == null) {
            mInstance = new CBBusinessLogic(manager);
        }
        return mInstance;
    }

    // App-specific CB-lite 1.4.4 View for "entries"
    //
    private Boolean initEntriesView() {
        if (mCbManager != null) { // needed?
            Database db = mCbManager.getDatabase();
            View view = db.getView("entries");
            view.setMap(new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    if (document.get("doc").equals("ent")) { // is entry
                        List<Object> key = new ArrayList<Object>();
                        key.add(document.get("when")); // keys.get(0)
                        key.add(document.get("labelId")); // keys.get(1)
                        emitter.emit(key, null);
                    }
                }
            }, "3");
            return true;
        } else {
            return false;
        }
    }

    // App-specific CB-lite 1.4.4 Query on View for "entries"
    //
    // TODO: change signature for PAGINATION
    // SEE: https://blog.couchbase.com/pagination-couchbase/
    //
    // See also: https://blog.couchbase.com/startkeydocid-behaviour/
    // which explains:
    // Here is a quick list of the important things to remember about startkey_docid:
    // startkey_docid will be entirely ignored if startkey is ommitted.
    // startkey_docid will only function correctly if you specify a startkey which exactly matches documents which are indexed in the view.
    // startkey_docid is expected to exactly match one of the docid's in the results which exactly match your startkey. If no match is made, the results will begin at the following key.
    //
    // TODO: limit=rowPerPage, skip=skip, startkey=currentStartkey, startkey_docid=startDocId
    // e.g., getEntries(int rowPerPage, String startkey, String startkeyDocID)
    // ACTUALLY, I think I can just use startKey, because it is a timestamp plus label
    // e.g., getEntries(int rowPerPage, Object startkey)
    // And the client would have to know to pass a key of List<Object>([when,labelId])...?
    // BETTER YET:
    //
    // e.g., getEntries(int rowPerPage, Datetime when, String labelId)...
    //
    // and let getEntries build the startKey as 
    //   List<Object> startKey = new ArrayList<Object>();
    //                     startKey.add(document.get("when"));
    // If labelId is NULL, then we assume it's "All Entries and pass in just the "when"; otherwise:
    //                     startKey.add(document.get("labelId"));
    // Also, if "when" is null, then we assume it is the first call and Skip = 0
    // Else Skip = 1;
    // Also, if labelId is not null, we have to build a Predicate<QueryRow>, e.g., ByLabel. (see by Category)
    // I assume Skip will happen AFTER predicate is applied?

    public class ByLabel implements Predicate<QueryRow> {
        private String uuid;
        public ByLabel(String labelId) {
            this.uuid = labelId;
        }
        public boolean apply(QueryRow row) {
            // Weird that this is the way you have to do it if key is array..
            LazyJsonArray<Object> keys = (LazyJsonArray<Object>)row.getKey();
            if (this.uuid.equals((String)keys.get(1))) { // labelId
                return true;
            } else {
                return false;
            }
        }
    }

    private ArrayList<Map<String, Object>> getEntriesForLabel(int rowsPerPage, String startDate, String labelId) {
        ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        if (mCbManager != null) { // needed?
            Database db = mCbManager.getDatabase();
            Query query = db.getView("entries").createQuery();
            query.setDescending(true);
            List<Object> startKey = new ArrayList<Object>();
            if (startDate != null) {
                startKey.add(startDate);
            } else {
                startKey.add(null); // so key array index is correct
            }
            if (labelId != null) {
                startKey.add(labelId);
                query.setPostFilter(new ByLabel(labelId));
            }
            if (startDate != null) {
                query.setStartKey(startKey);
            }
            // query.setStartKey(startKey); // first doc must be 1 *after* this one
            // default skip == 0
            query.setLimit(rowsPerPage);
            // System.out.println("CB rowsPerPager " + rowsPerPage);
            // System.out.println("CB startDate " + startDate);
            // System.out.println("CB labelId " + labelId);
            // System.out.println("CB startkey " + startKey);
            try {
                QueryEnumerator result = query.run();
                System.out.println("in qery, result: " + result.toString());
                for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                    QueryRow row = it.next();
                    // if (row.getConflictingRevisions().size() > 0) {
                    //     Log.w("MYAPP", "Conflict in document: %s", row.getDocumentId());
                    //     beginConflictResolution(row.getDocument());
                    // }
                    Document doc = row.getDocument();
                    // Couchbase Lite may have changed the client side file and hashed filename
                    // of any attachment, because it may habe sync'd with a doc that was modified
                    // by another client; who e.g., replaced the same attachment name with a 
                    // different file, which would result in a different hash and different filename. 
                    // Therefore, we load the client-side location of the attachment, just-in-time:
                    Revision rev = doc.getCurrentRevision();           
                    Attachment att = rev.getAttachment("entry.jpg"); // TODO: Pass in
                    if (att != null) {
                        URL url = att.getContentURL(); // cb-lite file path to attachment
                        String urlStr = url.toString();
                        String androidPath = urlStr.substring(5, urlStr.length()); // "file:..."
                        Map<String, Object> props = new HashMap<String, Object>();
                        props.putAll(doc.getProperties());
                        props.put("blobURL", androidPath); // just-in-time
                        props.put("localImagePath", androidPath); // temp
                        results.add(props);
                    } else {
                        results.add(doc.getProperties());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    // TODO: Perhaps, should index cat docs on "when", "title"
    // so that newest years are at the top. (vs leave alphabetically as it is?)
    private Boolean initCategoriesView() {
        if (mCbManager != null) { // needed?
            Database db = mCbManager.getDatabase();
            View view = db.getView("categories");
            view.setMap(new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    if (document.get("doc").equals("cat")) { // is category
                        List<Object> key = new ArrayList<Object>();
                        key.add(document.get("title"));
                        emitter.emit(key, null);
                    }
                }
            }, "2");
            return true;
        } else {
            return false;
        }
    }

    private ArrayList<Map<String, Object>> getCategories() {
        ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        if (mCbManager != null) { // needed?
            Database db = mCbManager.getDatabase();
            Query query = db.getView("categories").createQuery();
            // query.setDescending(false);
            try {
                QueryEnumerator result = query.run();
                for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                    QueryRow row = it.next();
                    results.add(row.getDocument().getProperties());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return results;
    }
   
    private Boolean initLabelsView() {
        if (mCbManager != null) { // needed?
            Database db = mCbManager.getDatabase();
            View view = db.getView("labels");
            view.setMap(new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    if (document.get("doc").equals("lab")) { // is label
                        List<Object> key = new ArrayList<Object>();
                        key.add(document.get("categoryId"));
                        key.add(document.get("title"));
                        emitter.emit(key, null); 
                    }
                }
            }, "2");
            return true;
        } else {
            return false;
        }
    }

    public class ByCategory implements Predicate<QueryRow> {
        private String uuid;
        public ByCategory(String categoryId) {
            this.uuid = categoryId;
        }
        public boolean apply(QueryRow row) {
            // Weird that this is the way you have to do it if key is array..
            LazyJsonArray<Object> keys = (LazyJsonArray<Object>)row.getKey();
            if (this.uuid.equals((String)keys.get(0))) { // categoryId
                return true;
            } else {
                return false;
            }
        }
    }

    private ArrayList<Map<String, Object>> getLabels(String categoryId) {
        ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        if (mCbManager != null) { // needed?
            Database db = mCbManager.getDatabase();
            Query query = db.getView("labels").createQuery();
            query.setPostFilter(new ByCategory(categoryId));
            // query.setDescending(false);
            try {
                QueryEnumerator result = query.run();
                for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                    QueryRow row = it.next();
                    results.add(row.getDocument().getProperties());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    private Boolean initPapersView() {
        if (mCbManager != null) { // needed?
            Database db = mCbManager.getDatabase();
            View view = db.getView("papers");
            view.setMap(new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    if (document.get("doc").equals("pap")) { // is paper
                        List<Object> key = new ArrayList<Object>();
                        key.add(document.get("when"));
                        emitter.emit(key, null);
                    }
                }
            }, "2");
            return true;
        } else {
            return false;
        }
    }

    private ArrayList<Map<String, Object>> getPapers() {
        ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        if (mCbManager != null) { // needed?
            Database db = mCbManager.getDatabase();
            Query query = db.getView("papers").createQuery();
            query.setDescending(true); // newest at top
            try {
                QueryEnumerator result = query.run();
                for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                    QueryRow row = it.next();
                    results.add(row.getDocument().getProperties());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    public void handleCall(MethodCall call, Result result) {
        switch (call.method) {
            case ("initView"):
                String view = call.arguments();
                switch(view) {
                    case ("entries"):
                        result.success(initEntriesView());
                        break;
                    case ("categories"):
                        result.success(initCategoriesView());
                        break;
                    case ("labels"):
                        result.success(initLabelsView());
                        break;
                    case ("papers"):
                        result.success(initPapersView());
                        break;
                    default:
                        result.success(false);    
                }
                break;
            case ("getEntriesForLabel"):
                int rowsPerPage = call.argument("rowsPerPage");
                String startDate = call.argument("startDate");
                String labelId = call.argument("labelId");
                result.success(getEntriesForLabel(rowsPerPage, startDate, labelId));
                break;
            case ("getCategories"):
                result.success(getCategories());
                break;
            case ("getLabels"):
                String categoryId = call.argument("categoryId");
                result.success(getLabels(categoryId));
                break;
            case ("getPapers"):
                result.success(getPapers());
                break;
            default:
                result.notImplemented();
        }
    }
}
