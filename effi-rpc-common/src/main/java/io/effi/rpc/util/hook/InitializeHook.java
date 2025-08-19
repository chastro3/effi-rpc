package io.effi.rpc.util.hook;

/**
 * Defines hooks for intercepting initialization events.
 * <p>
 * Provides callback methods that are invoked during the initialization
 * process of target objects, allowing customization before and after
 * initialization completes.
 */
public interface InitializeHook<T> extends Hook<T> {

   /**
    * Invoked before the target is initialized.
    */
   default void onInitializing(T target) {
   }

   /**
    * Invoked after the target has been initialized.
    */
   default void onInitialized(T target) {
   }

}
