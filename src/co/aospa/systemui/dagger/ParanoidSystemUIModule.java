/*
 * Copyright (C) 2019 The Android Open Source Project
 * Copyright (C) 2023 Paranoid Android
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.aospa.systemui.dagger;

import static com.android.systemui.Dependency.ALLOW_NOTIFICATION_LONG_PRESS_NAME;
import static com.android.systemui.Dependency.LEAK_REPORT_EMAIL_NAME;

import android.content.Context;
import android.hardware.SensorPrivacyManager;
import android.os.Handler;

import com.android.internal.logging.UiEventLogger;
import com.android.keyguard.KeyguardViewController;
import com.android.systemui.battery.BatterySaverModule;
import com.android.systemui.biometrics.FingerprintInteractiveToAuthProvider;
import com.android.systemui.controls.controller.ControlsTileResourceConfiguration;
import com.android.systemui.dagger.SysUISingleton;
import com.android.systemui.dagger.qualifiers.Main;
import com.android.systemui.dock.DockManager;
import com.android.systemui.dock.DockManagerImpl;
import com.android.systemui.doze.DozeHost;
import com.android.systemui.media.dagger.MediaModule;
import com.android.systemui.navigationbar.gestural.GestureModule;
import com.android.systemui.plugins.qs.QSFactory;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.power.dagger.PowerModule;
import com.android.systemui.qs.dagger.QSModule;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsImplementation;
import com.android.systemui.rotationlock.RotationLockModule;
import com.android.systemui.screenshot.ReferenceScreenshotModule;
import com.android.systemui.shade.NotificationShadeWindowControllerImpl;
import com.android.systemui.shade.ShadeController;
import com.android.systemui.shade.ShadeControllerImpl;
import com.android.systemui.shade.ShadeExpansionStateManager;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationLockscreenUserManagerImpl;
import com.android.systemui.statusbar.NotificationShadeWindowController;
import com.android.systemui.statusbar.dagger.StartCentralSurfacesModule;
import com.android.systemui.statusbar.events.StatusBarEventsModule;
import com.android.systemui.statusbar.notification.collection.provider.VisualStabilityProvider;
import com.android.systemui.statusbar.notification.collection.render.GroupMembershipManager;
import com.android.systemui.statusbar.phone.DozeServiceHost;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.policy.AccessibilityManagerWrapper;
import com.android.systemui.statusbar.policy.AospPolicyModule;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.DeviceProvisionedControllerImpl;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.HeadsUpManagerLogger;
import com.android.systemui.statusbar.policy.IndividualSensorPrivacyController;
import com.android.systemui.statusbar.policy.IndividualSensorPrivacyControllerImpl;
import com.android.systemui.statusbar.policy.SensorPrivacyController;
import com.android.systemui.statusbar.policy.SensorPrivacyControllerImpl;

import javax.inject.Named;

import co.aospa.systemui.biometrics.FingerprintInteractiveToAuthProviderImpl;
import co.aospa.systemui.controls.AospaControlsTileResourceConfigurationImpl;
import co.aospa.systemui.qs.tileimpl.ParanoidQSFactoryImpl;
import co.aospa.systemui.qs.tileimpl.ParanoidQSModule;
import co.aospa.systemui.volume.dagger.ParanoidVolumeModule;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Fork of {@link ReferenceSystemUIModule}
 */
@Module(includes = {
        AospPolicyModule.class,
        BatterySaverModule.class,
        GestureModule.class,
        MediaModule.class,
        PowerModule.class,
        QSModule.class,
        ReferenceScreenshotModule.class,
        RotationLockModule.class,
        StartCentralSurfacesModule.class,
        StatusBarEventsModule.class,
        ParanoidQSModule.class,
        ParanoidVolumeModule.class
})
public abstract class ParanoidSystemUIModule {

    @SysUISingleton
    @Provides
    @Named(LEAK_REPORT_EMAIL_NAME)
    static String provideLeakReportEmail() {
        return "";
    }

    @Binds
    abstract NotificationLockscreenUserManager bindNotificationLockscreenUserManager(
            NotificationLockscreenUserManagerImpl notificationLockscreenUserManager);

    @Provides
    @SysUISingleton
    static SensorPrivacyController provideSensorPrivacyController(
            SensorPrivacyManager sensorPrivacyManager) {
        SensorPrivacyController spC = new SensorPrivacyControllerImpl(sensorPrivacyManager);
        spC.init();
        return spC;
    }

    @Provides
    @SysUISingleton
    static IndividualSensorPrivacyController provideIndividualSensorPrivacyController(
            SensorPrivacyManager sensorPrivacyManager) {
        IndividualSensorPrivacyController spC = new IndividualSensorPrivacyControllerImpl(
                sensorPrivacyManager);
        spC.init();
        return spC;
    }

    /** */
    @Binds
    @SysUISingleton
    public abstract QSFactory bindQSFactory(ParanoidQSFactoryImpl qsFactoryImpl);

    @Binds
    abstract DockManager bindDockManager(DockManagerImpl dockManager);

    @Binds
    abstract ShadeController provideShadeController(ShadeControllerImpl shadeController);

    @SysUISingleton
    @Provides
    @Named(ALLOW_NOTIFICATION_LONG_PRESS_NAME)
    static boolean provideAllowNotificationLongPress() {
        return true;
    }

    @SysUISingleton
    @Provides
    static HeadsUpManagerPhone provideHeadsUpManagerPhone(
            Context context,
            HeadsUpManagerLogger headsUpManagerLogger,
            StatusBarStateController statusBarStateController,
            KeyguardBypassController bypassController,
            GroupMembershipManager groupManager,
            VisualStabilityProvider visualStabilityProvider,
            ConfigurationController configurationController,
            @Main Handler handler,
            AccessibilityManagerWrapper accessibilityManagerWrapper,
            UiEventLogger uiEventLogger,
            ShadeExpansionStateManager shadeExpansionStateManager) {
        return new HeadsUpManagerPhone(
                context,
                headsUpManagerLogger,
                statusBarStateController,
                bypassController,
                groupManager,
                visualStabilityProvider,
                configurationController,
                handler,
                accessibilityManagerWrapper,
                uiEventLogger,
                shadeExpansionStateManager
        );
    }

    @Binds
    abstract HeadsUpManager bindHeadsUpManagerPhone(HeadsUpManagerPhone headsUpManagerPhone);

    @Provides
    @SysUISingleton
    static Recents provideRecents(Context context, RecentsImplementation recentsImplementation,
            CommandQueue commandQueue) {
        return new Recents(context, recentsImplementation, commandQueue);
    }

    @SysUISingleton
    @Provides
    static DeviceProvisionedController bindDeviceProvisionedController(
            DeviceProvisionedControllerImpl deviceProvisionedController) {
        deviceProvisionedController.init();
        return deviceProvisionedController;
    }

    @Binds
    abstract KeyguardViewController bindKeyguardViewController(
            StatusBarKeyguardViewManager statusBarKeyguardViewManager);

    @Binds
    abstract NotificationShadeWindowController bindNotificationShadeController(
            NotificationShadeWindowControllerImpl notificationShadeWindowController);

    @Binds
    abstract DozeHost provideDozeHost(DozeServiceHost dozeServiceHost);

    @Binds
    abstract ControlsTileResourceConfiguration bindControlsTileResourceConfiguration(AospaControlsTileResourceConfigurationImpl configuration);

    @Binds
    abstract FingerprintInteractiveToAuthProvider bindFingerprintInteractiveToAuthProviderImpl(FingerprintInteractiveToAuthProviderImpl impl);
}
