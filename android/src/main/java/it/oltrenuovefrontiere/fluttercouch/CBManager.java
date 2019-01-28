package it.oltrenuovefrontiere.fluttercouch;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
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

// SEE: https://docs.couchbase.com/couchbase-lite/1.4/java.html#manager

// TODO: Bring back replicator functionality for cb 1.4.4


class ReplicatorConfiguration {
    URL url;
    String synctype; // PUSH, PULL, PUSH_AND_PULL
    Boolean continuous;
    Authenticator auth;
    Database database;
    public ReplicatorConfiguration( URL url, 
                                    String type, 
                                    Boolean continuous, 
                                    Authenticator auth,
                                    Database database) {
        this.url = url;
        this.synctype = type;
        this.continuous = continuous;
        this.auth = auth;
        this.database = database;
    }
}

class Replicator {
    private ReplicatorConfiguration mConfig = null;
    private Replication mPush = null;
    private Replication mPull = null;
    public Boolean isConfigured() {
        return !(mConfig == null);
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
        if (mPush != null) mPush.setContinuous(mConfig.continuous);
        if (mPull != null) mPull.setContinuous(mConfig.continuous);
    }

    void start() {
        if (mPush != null) mPush.start();
        if (mPull != null) mPull.start();
    }

    void stop() {
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
        if (_config.containsKey("username") 
                && _config.containsKey("password")
                && _config.containsKey("url")
                && _config.containsKey("synctype")
                && _config.containsKey("continuous")) {
            
            BasicAuthenticator auth = new BasicAuthenticator(_config.get("username"), _config.get("password"));
            ReplicatorConfiguration repConfig = new ReplicatorConfiguration(
                new URL(_config.get("url")),
                _config.get("synctype"),
                _config.get("continuous") == "true" ? true : false,
                auth,
                mDatabase.get(defaultDatabase));

            Replicator replicator = mReplicator.get(defaultDatabase);
            replicator.init(repConfig);
       } else {
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
