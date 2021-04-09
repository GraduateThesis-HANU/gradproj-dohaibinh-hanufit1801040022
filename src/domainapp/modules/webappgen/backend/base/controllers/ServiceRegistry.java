package domainapp.modules.webappgen.backend.base.controllers;

import domainapp.modules.webappgen.backend.base.services.CrudService;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"rawtypes"})
public final class ServiceRegistry {
    private static ServiceRegistry INSTANCE;
    public static ServiceRegistry getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServiceRegistry();
        }
        return INSTANCE;
    }

    private final Map<String, CrudService> serviceTypeMap;

    private ServiceRegistry() {
        this.serviceTypeMap = new ConcurrentHashMap<>();
    }

    public CrudService get(String type) {
        for (Map.Entry<String, CrudService> entry : serviceTypeMap.entrySet()) {
            if (entry.getKey().toLowerCase(Locale.ROOT)
                    .contains(type.toLowerCase(Locale.ROOT).concat("service"))) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void put(String genericType, CrudService serviceInstance) {
        this.serviceTypeMap.put(genericType, serviceInstance);
    }
}
