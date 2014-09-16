package de.draexlmaier.bpm.process.repro;

import javax.ejb.Local;

@Local
public interface TaskPollingEJBLocal
{
    void pollNow();
}
