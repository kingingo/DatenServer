package me.kingingo.kDaten.api.EventManager;
public abstract interface Cancellable
{
  public abstract boolean isCancelled();
  public abstract void setCancelled(boolean paramBoolean);
}