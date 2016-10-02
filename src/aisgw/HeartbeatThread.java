
package aisgw;

/**
 *
 * @author DMcG
 */
public class HeartbeatThread implements Runnable
{
    @Override
    public void run()
    {
       ShipDatabase shipDatabase = ShipDatabase.getInstance();
       
       shipDatabase.doHeartbeat();
       try
       {
            Thread.sleep(10000);
       }
       catch(Exception e)
       {
           System.out.println("Unquiet sleep");
       }
    }

}
