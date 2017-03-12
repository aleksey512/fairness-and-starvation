package info.biosfood.fairness.and.starvation;

import org.apache.log4j.Logger;

public class AbstractTest {

    public Runnable createReadJob(final Logger LOG, final UnfairObjectAccess subject) {
        return () -> {
            int value = subject.getValue();
            if(value != 101) {
                LOG.debug("Read a default value");
            } else {
                LOG.debug("A value has been updated");
            }
        };
    }

    public Runnable createWriteJob(final Logger LOG, final UnfairObjectAccess subject) {
        return () -> {
            LOG.debug("Setting a new value");
            subject.setValue(101);
            LOG.debug("Done setting the new value");
        };
    }

}
