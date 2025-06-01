package io.effi.rpc.base.repository;

import io.effi.rpc.base.RemoteClient;
import io.effi.rpc.base.RemoteService;

import java.util.ArrayList;
import java.util.List;

/**
 * Monitor Manager.
 */
public class MonitorManager {

    private List<RemoteService<?>> remoteServices = new ArrayList<>();

    private List<RemoteClient<?>> remoteClients = new ArrayList<>();

}
