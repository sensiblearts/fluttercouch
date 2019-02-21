package it.oltrenuovefrontiere.fluttercouch;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Revision;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.Attachment;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.auth.Authenticator;
import com.couchbase.lite.auth.BasicAuthenticator;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.util.Log;

import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

// SEE: https://docs.couchbase.com/couchbase-lite/1.4/java.html#manager

class ReplicatorConfiguration {
    String token;
    String username;
    URL url;
    String synctype; // PUSH, PULL, PUSH_AND_PULL
    Boolean continuous;
    Authenticator auth;
    Database database;
    public ReplicatorConfiguration( String token,
                                    String username,
                                    URL url, 
                                    String type, 
                                    Boolean continuous, 
                                    Authenticator auth,
                                    Database database) {
        this.token = token;
        this.username = username;
        this.url = url;
        this.synctype = type;
        this.continuous = continuous;
        this.auth = auth; // auth is null
        this.database = database;
    }
}

class Replicator {
    private ReplicatorConfiguration mConfig = null;
    private Replication mPush = null;
    private Replication mPull = null;
    private ReplicationEventListener_1_4 mEventSpy = null;
    public Boolean isConfigured() {
        return !(mConfig == null);
    }
    ListenerToken addChangeListener(ReplicationEventListener_1_4 eventSpy) {
        mEventSpy = eventSpy;
        // TODO: generate a UNIQUE token and keep a map of listeners
        return new ListenerToken("aToken");
    }
    void removeChangeListener(ListenerToken token) {
        // TODO: use map of listeneers to remove
        mEventSpy = null; // TODO: couchbase remove any remaining listeners
    }
    // call default constructor, then init().
    // TODO: enable authenticators or use the token header thing
    void init(ReplicatorConfiguration config) {
        mConfig = config;
        switch(mConfig.synctype) {
            case "PUSH":
                mPush = mConfig.database.createPushReplication(mConfig.url);
                // mPush.setAuthenticator(mConfig.auth);
            break;
            case "PULL":
                mPull = mConfig.database.createPullReplication(mConfig.url);
                // mPull.setAuthenticator(mConfig.auth);
            break;
            case "PUSH_AND_PULL":
                mPush = mConfig.database.createPushReplication(mConfig.url);
                // mPush.setAuthenticator(mConfig.auth);
                mPull = mConfig.database.createPullReplication(mConfig.url);
                // mPull.setAuthenticator(mConfig.auth);
            break;
        }
        // These headers are for the couchdb "database per user" approach,
        // where user nameand token are passed in headers.
        System.out.println("USERNAME");
        System.out.println(mConfig.username);
        System.out.println("token");
        System.out.println(mConfig.token);
        
        if (mPush != null) {
            mPush.setContinuous(mConfig.continuous);
            Map<String, Object> pushHeaders = mPush.getHeaders();
            pushHeaders.put("X-Auth-CouchDB-UserName", mConfig.username);
            pushHeaders.put("X-Auth-CouchDB-Token",mConfig.token);
            pushHeaders.put("Content-Type","application/json; charset=utf-8");
            mPush.setHeaders(pushHeaders);
        }
        if (mPull != null) {
            mPull.setContinuous(mConfig.continuous);
            Map<String, Object> pullHeaders = mPull.getHeaders();
            pullHeaders.put("X-Auth-CouchDB-UserName", mConfig.username);
            pullHeaders.put("X-Auth-CouchDB-Token",mConfig.token);
            pullHeaders.put("Content-Type","application/json; charset=utf-8");
            mPull.setHeaders(pullHeaders);
        }
    }
    void start() {
         if (mPush != null) {
            if (mEventSpy != null) {
                mPush.addChangeListener(new Replication.ChangeListener() {
                    @Override
                    public void changed(Replication.ChangeEvent event) {
                        mEventSpy.changed(event);
                    }
                });
            }
            mPush.start();
        }
        if (mPull != null) {
            if (mEventSpy != null) {
                mPull.addChangeListener(new Replication.ChangeListener() {
                    @Override
                    public void changed(Replication.ChangeEvent event) {
                        mEventSpy.changed(event);
                    }
                });
            }
            mPull.start();
        }
    }
    void stop() {
        // TODO: remove change listeners?
        if (mPush != null) mPush.stop();
        if (mPull != null) mPull.stop();
    }
}

public class CBManager {
    private static AndroidContext _context = null;
    private Manager _manager = null;
    
    private static CBManager mInstance = null;
    private HashMap<String, Database> mDatabase = new HashMap<>();
    private HashMap<String, Replicator> mReplicator = new HashMap<>();

    private String defaultDatabase = "defaultdatabase";

    private CBManager(AndroidContext context) {
             try {
                _manager = new Manager(context, Manager.DEFAULT_OPTIONS);
             } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public static CBManager getInstance(AndroidContext context) {
        if (mInstance == null) {
            _context = context;
            mInstance = new CBManager(context);
        }
        return mInstance;
    }

    public Database getDatabase() {
        return mDatabase.get(defaultDatabase);
    }

    public Database getDatabase(String name) {
        if (mDatabase.containsKey(name)) {
            return mDatabase.get(name);
        }
        return null;
    }

    public Map<String, String> saveDocument(Map<String, Object> _map) throws CouchbaseLiteException {
        HashMap<String, String> resultMap = new HashMap<String, String>();
        Document doc = mDatabase.get(defaultDatabase).createDocument();
        doc.putProperties(_map);
        resultMap.put("_id", doc.getId());
        resultMap.put("_rev", (String)doc.getProperties().get("_rev"));
        return resultMap;
    }

    public Map<String, String> saveDocumentWithId(String _id, Map<String, Object> _map) throws CouchbaseLiteException {
        HashMap<String, String> resultMap = new HashMap<String, String>();
        Document doc = mDatabase.get(defaultDatabase).getDocument(_id);
        doc.putProperties(_map);
        resultMap.put("_id", doc.getId());
        resultMap.put("_rev", (String)doc.getProperties().get("_rev"));
        return resultMap;
    }
    // TODO: Are update and save equivalent?
    public Map<String, String> updateDocumentWithId(String _id, Map<String, Object> _map) throws CouchbaseLiteException {
        HashMap<String, String> resultMap = new HashMap<String, String>();
        Document doc = mDatabase.get(defaultDatabase).getDocument(_id);
        // TODO: doc valid? exist?
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.putAll(_map);
        try {
            doc.putProperties(properties);
            resultMap.put("_id", doc.getId());
            resultMap.put("_rev", (String)doc.getProperties().get("_rev"));
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            resultMap.put("_id", null);
            resultMap.put("_rev", null);
        }
        return resultMap;
    }

    public Map<String, Object> getDocumentWithId(String _id) throws CouchbaseLiteException {
        Database defaultDb = mDatabase.get(defaultDatabase);
        HashMap<String, Object> resultMap = new HashMap<String, Object>();
        if (defaultDb != null) {
            try {
                Document document = defaultDb.getDocument(_id);
                if (document != null) {
                    resultMap.put("doc", document.getProperties());
                    // resultMap.put("doc", document.toMap());
                    resultMap.put("id", _id);
                } else {
                    resultMap.put("doc", null);
                    resultMap.put("id", _id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultMap;
    }

    public boolean deleteDocumentWithId(String _id) throws CouchbaseLiteException {
        Document doc = mDatabase.get(defaultDatabase).getDocument(_id);
        return doc.delete();
    }

    public Map<String, String> upsertNamedAttachmentAsFilepath(String _id, Map<String, Object> _map) throws CouchbaseLiteException, Exception {
        HashMap<String, String> resultMap = new HashMap<String, String>();
        if (        _map.containsKey("mime") 
                    && _map.containsKey("attachName")
                    && _map.containsKey("filePath")) {
            String mime = _map.get("mime").toString();
            String attachName = _map.get("attachName").toString();
            String filePath = _map.get("filePath").toString();
            Document doc = mDatabase.get(defaultDatabase).getDocument(_id);
            resultMap.put("_id", doc.getId());
            resultMap.put("_rev", (String)doc.getProperties().get("_rev")); // before attach
            resultMap.put("attachName", null);
            UnsavedRevision newRev = doc.getCurrentRevision().createRevision();
            try {
                File file = new File(filePath);
                InputStream stream = new FileInputStream(file);
                newRev.setAttachment(attachName, mime, stream);
                SavedRevision savedRev = newRev.save(); // save once
                // After save() is called above, the file now has a physical location
                // in a private couchbase-lite folder; which you can get by calling
                // Attachment att = savedRev.getAttachment(attachName);
                // URL url = att.getContentURL(); // cb-lite file path to attachment
                resultMap.put("_id", doc.getId());
                resultMap.put("_rev", (String)doc.getProperties().get("_rev")); // after attach
                resultMap.put("attachName", attachName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new Exception();
        }      
        return resultMap;
    }

    public void initDatabaseWithName(String _name) throws CouchbaseLiteException {
       if (!mDatabase.containsKey(_name)) {
           defaultDatabase = _name;
           Database db = _manager.getDatabase(_name);
           mDatabase.put(_name, db);
       }
    }

    public void createReplicatorWithName(String _name) throws CouchbaseLiteException {
       if (!mReplicator.containsKey(_name)) {
           mReplicator.put(_name, new Replicator());
        // if it's there, stop it and delete it first?
       }
    }

    public String configureReplicator(Map<String, String> _config) throws Exception {
        if (    _config.containsKey("token") // for header, couch-per-user approach
                && _config.containsKey("username")
                && _config.containsKey("url")
                && _config.containsKey("synctype")
                && _config.containsKey("continuous")) {
            ReplicatorConfiguration repConfig = new ReplicatorConfiguration(
                _config.get("token"),
                _config.get("username"),
                new URL(_config.get("url")),
                _config.get("synctype"),
                _config.get("continuous").equals("true"),
                (Authenticator)null, // Need to look into couchbase 1.4 code to see if they have an authenticator that sets header tokens
                mDatabase.get(defaultDatabase));
            Replicator replicator = mReplicator.get(defaultDatabase);
            replicator.init(repConfig);
       } else {
           // else if you want to implement BasicAuthenticator logic, do it here
           throw new Exception();
       }
       return "what should this return?";
   }

   public void startReplicator() {
      Replicator replicator = mReplicator.get(defaultDatabase);
      replicator.start();
   }

   public void stopReplicator() {
       Replicator replicator = mReplicator.get(defaultDatabase);
        replicator.stop();
   }

   public Replicator getReplicator() {
        return mReplicator.get(defaultDatabase);
   }
}
