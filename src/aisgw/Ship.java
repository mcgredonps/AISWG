

package aisgw;

import dk.dma.ais.message.AisPosition;
import dk.dma.ais.message.AisPositionMessage;
import dk.dma.ais.message.AisStaticCommon;
import java.util.*;
import edu.nps.moves.dis.*;
import edu.nps.moves.disutil.*;


public class Ship 
{
    public static final long HEARTBEAT_INTERVAL = 10000;
    
    String shipName;
    Date lastAisPositionReport;
    Date lastEspduUpdate;
    AisPositionMessage lastPositionReport;
    AisStaticCommon lastStaticReport;
    
    EntityStatePdu espdu;

    public Ship(AisPositionMessage positionReport)
    {
        shipName = null;
        lastAisPositionReport = null;
        lastPositionReport = positionReport;
        lastStaticReport = null;
        espdu = new EntityStatePdu();
        
        MmsiToEid mapper = MmsiToEid.getInstance();
        EntityID anID = mapper.getEntityIDForMMSI(positionReport.getUserId());
        
        espdu.setEntityID(anID);
        // Come up with a more realistic way to set the entity type.
        // This is just a placeholder.
        espdu.getEntityType().setCountry(225);
        espdu.getEntityType().setEntityKind((short)1);
        espdu.getEntityType().setCategory((short)61);
        espdu.getEntityType().setDomain((short)3);
        espdu.getEntityType().setSubcategory((short)1);
        
    }
    
    public void setNewPositionReport(AisPositionMessage positionReport)
    {
        this.lastPositionReport = positionReport;
        
        AisPosition location = positionReport.getPos();
        double lat = location.getLatitudeDouble();
        double lon = location.getLongitudeDouble();
        
        // Defensive programming here. Live data is notoriously bad.
        // Do range-checking and any other sanity checks you can think
        // of on the data
        
        // Convert to DIS geocentric coordinates
        double[] xyz = CoordinateConversions.getXYZfromLatLonDegrees(lat, lon, 0.0);
        espdu.getEntityLocation().setX(xyz[0]);
        espdu.getEntityLocation().setY(xyz[1]);
        espdu.getEntityLocation().setZ(xyz[2]);
        
        this.sendEspdu();
    }
    
    public void setNewStaticReport(AisStaticCommon staticReport)
    {
        this.lastStaticReport = staticReport;
        String shipName = staticReport.getName();
        char[] truncatedName = new char[10];
        
        shipName.getChars(0, 10, truncatedName, 0);
        
        System.out.println("Original name:" + shipName + " Truncated name: " + new String(truncatedName));
        byte[] truncatedBytes = new byte[10];
        for(int idx = 0; idx < 10; idx++)
        {
            truncatedBytes[idx] = (byte)truncatedName[idx];
        }
        espdu.getMarking().setCharacters(truncatedBytes);
        
    }
    
    public boolean needsHeartbeat()
    {
        Date now = new Date();
        if(lastEspduUpdate == null)
        {
            lastEspduUpdate = new Date();
            return true;
        }
        if(lastEspduUpdate.getTime() + HEARTBEAT_INTERVAL < now.getTime())
            return true;
        
        return false;
    }
    
    public void sendEspdu()
    {
        Network network = Network.getInstance();
        network.sendPdu(espdu);
    }
}
