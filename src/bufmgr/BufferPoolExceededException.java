package bufmgr;
import chainexception.*;


/**
 * Created by Nicole on 2/14/16.
 */
public class BufferPoolExceededException extends ChainException{
    public BufferPoolExceededException (Exception e, String name)

    {
        super(e, name);
    }
}
