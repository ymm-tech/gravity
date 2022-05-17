package io.manbang.gravity.agent;

import io.manbang.gravity.plugin.GravityService;
import io.manbang.gravity.plugin.Services;
import lombok.extern.java.Log;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 重力服务启动器，所有的服务植入完成后执行
 *
 * @author dzhang
 * @since 2020/08/29 11:51:30
 */
@Log
enum GravityServiceBoot {
    /**
     * instance
     */
    INSTANCE;

    /**
     * 已经启动了的重力服务
     */
    private static final Set<GravityService> STARTED_GRAVITY_SERVICE = new HashSet<>();
    /**
     * 已经加载过的类加载器
     */
    private static final Set<ClassLoader> LOADED_CLASS_LOADER = new HashSet<>();

    /**
     * 不同的类加载，有可能会加载出相同的SPI，因为父子关系的存在，会导致重复启动服务，因此，缓存已经存在父 GravityService，
     * 已经启动的，就不再执行启动了。
     *
     * @param classLoader {@link GravityService} 的类加载器
     */
    public void startServices(ClassLoader classLoader) {
        // 已经加载了，就不在加载了
        if (LOADED_CLASS_LOADER.contains(classLoader)) {
            return;
        }

        // 标记此 ClassLoader 已经被加载过
        LOADED_CLASS_LOADER.add(classLoader);

        log.info("开始启动重力服务……");
        List<GravityService> gravityServices = getStartingServices(classLoader);

        doStartServices(classLoader, gravityServices);
        addStopServiceHook(gravityServices);
    }

    private List<GravityService> getStartingServices(ClassLoader classLoader) {
        List<GravityService> gravityServices = new LinkedList<>();

        for (GravityService gravityService : Services.loadAll(GravityService.class, classLoader)) {
            // 已经启动过的服务，直接忽略
            if (STARTED_GRAVITY_SERVICE.contains(gravityService)) {
                continue;
            }

            // 缓存还未加载过的服务
            STARTED_GRAVITY_SERVICE.add(gravityService);
            // 待启动的服务
            gravityServices.add(gravityService);
        }
        return gravityServices;
    }

    private void addStopServiceHook(List<GravityService> gravityServices) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (GravityService gravityService : gravityServices) {
                stopService(gravityService);
            }
        }));
    }

    private void stopService(GravityService gravityService) {
        try {
            gravityService.stop();
        } catch (Exception e) {
            log.warning(gravityService.getName() + " 服务停止失败");
            e.printStackTrace();
        }
    }

    private void doStartServices(ClassLoader classLoader, List<GravityService> gravityServices) {
        Thread thread = new Thread(() -> {
            for (GravityService service : gravityServices) {
                boolean prepared = service.prepare(classLoader);
                if (prepared) {
                    try {
                        service.start();
                    } catch (Exception e) {
                        log.warning(service.getName() + " 服务启动失败");
                        e.printStackTrace();
                        stopService(service);
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.setName("重力服务启动线程");
        thread.start();
    }

}
