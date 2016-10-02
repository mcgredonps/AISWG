
package aisgw;

import dk.dma.ais.reader.*;
import dk.dma.ais.message.*;

import edu.nps.moves.dis.*;
import edu.nps.moves.disutil.*;
import edu.nps.moves.spatial.*;

import java.util.function.*;
import java.util.*;
import java.util.concurrent.*;

public class AISGW {

    public static void main(String[] args) 
    {
        try
        {
            // Connect to the NPS server
            AisReader reader = AisReaders.createReader("172.20.70.143", 9010);
            
            ShipDatabase.getInstance();
            Thread heartbeatUpdateThread = new Thread(new HeartbeatThread());
            heartbeatUpdateThread.start();
            
          // Uses some fancy stuff from Java 8. 
            reader.registerHandler(new Consumer<AisMessage>() 
            {
                // We received an AIS messge
                
                @Override
                public void accept(AisMessage aisMessage) 
                {
                    //System.out.println("message id: " + aisMessage.getMsgId());

                    // There are several types of messages. For now just trap the
                    // position reports.
                    
                    switch(aisMessage.getMsgId())
                    {
                        // Message types 1, 2 and 3 are all variants of position reports
                        // See www.navcen.uscg.gov/?pageName=AISMessages
                        // Types 1, 2, and 3 are all position reports
                        case 1:
                        case 2:
                        case 3:
                            //System.out.println("Position report");
                            
                            // Cast it to a position report message type
                            AisPositionMessage positionMessage = (AisPositionMessage)aisMessage;

                            // Extract relevant data
                            
                            // Knots, 1/10 knot steps (0-1002.2 knots)
                            int speedOverGround = positionMessage.getSog();

                            // Lat/lon
                            AisPosition location = positionMessage.getPos();
                            double lat = location.getLatitudeDouble();
                            double lon = location.getLongitudeDouble();
                           

                            // MMSI number
                            int userId = aisMessage.getUserId();
                            
                            // In degrees; 511=not available/default
                            int heading = positionMessage.getTrueHeading();

                            
                           //System.out.println("ID: " + userId + 
                            //        " Location: " + location + 
                            //        " Speed, tenths of knots:" + speedOverGround +
                            //        " true heading:" + heading +
                            //        " lat: " + lat +
                            //        " lon:" + lon);
                            
                            int countryCode = (int)(userId / 1000000);
                            //System.out.println("MMSSI:" + userId + " Country code:" + countryCode);
                           
                            ShipDatabase ships = ShipDatabase.getInstance();
                            Ship aShip = ships.getShipFromMMSI(userId);
                            if(aShip == null)
                            {
                                aShip = new Ship(positionMessage); 
                                ships.addShip(userId, aShip);
                            }
                            else
                            {
                                //System.out.println("***Found existing ship " + userId);
                            }
                            
                            aShip.setNewPositionReport(positionMessage);
                                    
                            
                            break;

                        // Static position report, sometimes used for navigation aids
                        case 5:
                            //System.out.println("Static position report");
                            // Get the ship name here and insert that into
                            // the marking field of the PDU. The static reports
                            // come in much less frequently than position reports,
                            // and it's possible we have no entry for the static 
                            // report because a position report hasn't arrived yet. 
                            // We could also have an entry in the database for a 
                            // while before we receive a position report with a ship name.
                            
                            AisStaticCommon staticMessage = (AisStaticCommon)aisMessage;
                            
                            int mmsi = aisMessage.getUserId();
                            
                           
                            ShipDatabase shipdb = ShipDatabase.getInstance();
                            Ship ship = shipdb.getShipFromMMSI(mmsi);
                            if(ship == null)
                            {
                                break;
                            }
                            
                            ship.setNewStaticReport(staticMessage);
   
                            break;

                        default:
                            //System.out.println("other type of message");
                    }
                }
        });
        
        // Start reading from the AIS feed
        reader.start();
        reader.join();
        
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
}
