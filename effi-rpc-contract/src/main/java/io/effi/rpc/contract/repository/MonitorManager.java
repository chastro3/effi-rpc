package io.effi.rpc.contract.repository;

import io.effi.rpc.contract.RemoteClient;
import io.effi.rpc.contract.RemoteService;

import java.util.ArrayList;
import java.util.List;

/**
 * Monitor Manager.
 */
public class MonitorManager {

    private List<RemoteService<?>> remoteServices = new ArrayList<>();

    private List<RemoteClient<?>> remoteClients = new ArrayList<>();

}
