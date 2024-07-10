package ecscalibur.core

import ecscalibur.core.components.Component
import ecscalibur.core.components.ComponentType

import scala.annotation.targetName

export entity.*

object entity:
  /** Identifiers that do not hold any state. They can be created through a [[World]] instance,
    * which will then handle their lifetime and [[Component]]s.
    * @param id
    *   the unique ID of this Entity.
    */
  class Entity private[ecscalibur] (val id: Int):
    /** Syntactic sugar for [[World.update]].
      *
      * @param c
      *   the Component whose reference must be updated
      */
    @targetName("update")
    inline def <==(c: Component)(using World): Unit =
      summon[World].update(this, c)

    /** Syntactic sugar for [[World.hasComponents]].
      *
      * @param tpe
      *   the ComponentTypes of the Components this entity should have
      */
    @targetName("has")
    inline def ?>(tpe: ComponentType*)(using World): Boolean =
      summon[World].hasComponents(this, tpe*)

    /** Syntactic sugar for [[Mutator.defer]] [[EntityRequest.removeComponent]].
      *
      * @param tpe
      *   ComponentType of the Component to be removed
      * @param orElse
      *   callback executed if the deferred request fails
      * @return
      *   this Entity
      */
    @targetName("remove")
    inline def -=(tpe: ComponentType, inline orElse: () => Unit)(using World): Entity =
      val _ = summon[World].mutator defer EntityRequest.removeComponent(this, tpe, orElse)
      this

    /** Syntactic sugar for [[Mutator.defer]] [[EntityRequest.addComponent]].
      *
      * @param c
      *   Component to be added
      * @param orElse
      *   callback executed if the deferred request fails
      * @return
      *   this Entity
      */
    @targetName("add")
    inline def +=(c: Component, inline orElse: () => Unit)(using World): Entity =
      val _ = summon[World].mutator defer EntityRequest.addComponent(this, c, orElse)
      this

    override def equals(that: Any): Boolean = that match
      case e: Entity => id == e.id
      case _ => false

    override def hashCode(): Int = id.##
