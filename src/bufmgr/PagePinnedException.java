package bufmgr;
import chainexception.*;

/**
 * Created by Nicole on 2/14/16.
 */
public class PagePinnedException extends ChainException {
    public PagePinnedException (Exception e, String name)

    {
        super(e, name);
    }
}