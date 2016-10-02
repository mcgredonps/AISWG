
package aisgw;

import java.util.*;
import java.util.concurrent.*;
import edu.nps.moves.dis.*;

/**
 * Maps AIS mmsi numbers (9 digits) to a DIS entity ID.<p>
 * 
 * This is a singleton pattern. 
 * 
 * @author DMcG
 */
public class MmsiToEid 
{
    /** DIS Site ID in the entity ID */
    public static final int SITE_ID = 123;
    
    /** The app ID in the entity ID. Because there may be more ships
     * in the world than can be held in a 16 bit unsigned short, this
     * is only the starting point; entity IDs from the AIS source
     * may span multiple appIDs, for example 42 and 43.
     */
    public int appId = 42;
    
    /**
     * The next entity ID to use
     */
    public int nextId = 1;
    
    /** Singleton instance */
    static private MmsiToEid sharedInstance;
    
    /** Hash map that links MMSI's to DIS entity ID's. */
    ConcurrentHashMap<Integer, EntityID> mmsiToEid;
    
    /**
     * Private constructor; use getInstance to retrieve the single shared instance
     */
    private MmsiToEid()
    {
        mmsiToEid = new ConcurrentHashMap();
    }
    
    
    
    /**
     * Use this to return the single shared instance
     * @return singleton instance
     */
    public static synchronized MmsiToEid getInstance()
    {
        if(sharedInstance == null)
        {
            sharedInstance = new MmsiToEid();
        }
        
        return sharedInstance;
    }
    
    /**
     * Return a DIS entity ID for a given MMSI. If we've heard from
     * this MMSI before, return the already allocated DIS entity ID.
     * Otherwise, create a new DIS entity ID and save that.
     * @param mmsi
     * @return DIS entity ID
     */
    public EntityID getEntityIDForMMSI(int mmsi)
    {
        EntityID id = mmsiToEid.get(mmsi);
        if(id == null)
        {
            id = new EntityID();
            id.setSite(SITE_ID);
            id.setApplication(appId);
            id.setEntity(nextId++);
            System.out.println("New EID: (" + id.getSite() + ", " + id.getApplication() + ", " + id.getEntity() + ")");
            
            mmsiToEid.put(mmsi, id);
            
            // If too many , roll over and bump up the app ID
            if(nextId > 32000)
            {
                appId++;
                nextId = 1;
            }
        }

        //System.out.println("MMSI to EID mappings: " + mmsiToEid.size());
        return id;
        
    }

}
