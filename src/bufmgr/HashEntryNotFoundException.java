package bufmgr;
import chainexception.*;


/**
 * Created by Nicole on 2/14/16.
 */
public class HashEntryNotFoundException extends ChainException {
    public HashEntryNotFoundException (Exception e, String name)

    {
        super(e, name);
    }
}