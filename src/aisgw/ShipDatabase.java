package aisgw;

import java.util.*;
import java.util.concurrent.*;

/**
 * A singleton pattern. There should be one and only one instance
 * of the ShipDatabase object. Use getInstance() to retrieve it,
 * for example ShipDatabase.getInstance()
 * 
 * @author mcgredo
 */
public class ShipDatabase 
{
    /** Key is the mmsi, value is a ship object */
     private ConcurrentHashMap<Integer, Ship> ships;
     
     /** The single, shared instance */
     static ShipDatabase shipDatabase = null;
 
     /**
      * Private constructor. people outside the class can't call
      * it; only we can. This ensures they can get a ShipDatabase
      * object only by calling getInstance().
      */
    private ShipDatabase()
    {
        ships = new ConcurrentHashMap();  
    }
    
    /**
     * The only way to get an instance of the ShipDatabase object
     * @return Single, shared copy
     */
    static public synchronized ShipDatabase getInstance()
    {
        // First time we've called getInstance()? create a new
        // shipDatabase object. Otherwise, return the existing
        // shipDatabase object.
        
        if(shipDatabase == null)
        {
            shipDatabase = new ShipDatabase();
        }
        
        return shipDatabase;
    }
    
    /**
     * Given the MMSI, return the ship object associated with that.
     * If the ship does not exist, we return null.
     * @param mmsi
     * @return 
     */
    public Ship getShipFromMMSI(int mmsi)
    {
        return ships.get(mmsi);
    }
    
    /**
     * Add a new ship to the database, keyed by the MMSI for that ship.
     * @param mmsi
     * @param aShip 
     */
    public void addShip(int mmsi, Ship aShip)
    {
        ships.put(mmsi, aShip);
        System.out.println("Number of ships in database: " + ships.size());
    }
    
    /**
     * Examine every ship in the database, and send a heartbeat
     * ESDPU for that ship if warranted.
     */
    public void doHeartbeat()
    {
        System.out.println("Doing heartbeat cycle");
        Iterator it = ships.values().iterator();
        while(it.hasNext())
        {
            Ship aShip = (Ship)it.next();
            if(aShip.needsHeartbeat())
            {
                aShip.sendEspdu();
            }
            
        }
    }

}
