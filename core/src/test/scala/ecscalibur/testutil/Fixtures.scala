package ecscalibur

import ecscalibur.core.*
import ecscalibur.core.archetype.ArchetypeManager
import ecscalibur.core.archetype.Signature
import ecscalibur.core.archetype.archetypes.Aggregate
import ecscalibur.core.archetype.archetypes.Fragment
import ecscalibur.core.context.MetaContext
import ecscalibur.core.world.World
import ecsutil.ProgressiveMap

// 'import core.archetype.Archetypes.Archetype.DefaultFragmentSizeBytes' warns about an unused import
// for some reason.
inline val DefaultFragmentSize = core.archetype.archetypes.Archetype.DefaultFragmentSize

object fixtures:
  import ecscalibur.testutil.testclasses.Value

  class TestMutator extends Mutator:
    override def defer(q: DeferredRequest): Boolean = false
    override def doImmediately(q: ImmediateRequest): Boolean = false

  class ArchetypeManagerFixture(entityComponents: Seq[Component]*):
    require(entityComponents.length > 0)

    val entitiesCount = entityComponents.length
    val archManager = ArchetypeManager()
    val context = MetaContext()
    val mutator = TestMutator()
    val entities = (0 until entityComponents.length).map(Entity(_))
    for (comps, idx) <- entityComponents.zipWithIndex do archManager.addEntity(entities(idx), comps*)

  class IterateNFixture(nEntities: Int = 100, extraComponents: Seq[Component] = Seq.empty):
    require(nEntities > 0)

    val archManager = ArchetypeManager()
    val context = MetaContext()
    val testValue = Value(10)
    val mutator = TestMutator()
    private val entities: Vector[Entity] = (0 until nEntities).map(Entity(_)).toVector
    private val values: Vector[Value] = (0 until nEntities).map(Value(_)).toVector
    private var sum = 0

    for (e, idx) <- entities.zipWithIndex do
      archManager.addEntity(e, (values(idx) +: extraComponents.toArray)*)

    def onIterationStart(v: Value) = sum += v.x
    def isSuccess = sum == values.map(_.x).sum + nEntities * testValue.x

  private[fixtures] abstract class ArchetypeFixture(components: Component*)(nEntities: Int):
    val componentIds = components.map(_.typeId)
    val entities = (0 until nEntities).map(Entity(_)).toVector
    val nextEntity = Entity(entities.length)

  class StandardArchetypeFixture(components: Component*)(
      nEntities: Int = 100,
      fragmentSize: Long = DefaultFragmentSize
  ) extends ArchetypeFixture(components*)(nEntities):
    val archetype = Aggregate(Signature(componentIds*))(fragmentSize)
    for e <- entities do archetype.add(e, components*)

  class StandardFragmentFixture(components: Component*)(
      nEntities: Int = 100,
      maxEntities: Int = 100
  ) extends ArchetypeFixture(components*)(nEntities):
    require(nEntities <= maxEntities)
    val signature = Signature(componentIds*)
    val fragment =
      Fragment(signature, ProgressiveMap.from(signature.underlying.toArray*), maxEntities)
    for e <- entities do fragment.add(e, components*)

  class SystemFixture(nEntities: Int = 1):
    val world = World()
    val defaultValue = Value(1)
    for i <- (0 until nEntities) do world.archetypeManager.addEntity(Entity(i), defaultValue)
