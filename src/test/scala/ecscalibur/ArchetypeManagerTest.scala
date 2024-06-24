package ecscalibur.core

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.*
import component.*
import CSeq.Extensions.*
import component.Annotations.component
import ecscalibur.core.archetype.any
import archetype.ArchetypeManager

class ArchetypeManagerTest extends AnyFlatSpec with should.Matchers:
  @component
  case class Value(x: Int) extends Component
  object Value extends ComponentType

  @component
  class C1 extends Component
  object C1 extends ComponentType

  @component
  class C2 extends Component
  object C2 extends ComponentType

  "An archetype manager" should "correctly add new entities and iterate over them" in:
    val am = ArchetypeManager()
    val (v1, v2) = (Value(1), Value(2))
    val toAdd = Map(
      Entity(0) -> CSeq(v1, C2()),
      Entity(1) -> CSeq(v2, C1(), C2())
    )
    for (entity, comps) <- toAdd do am.addEntity(entity, comps)
    var sum = 0
    am.iterateReading(archetype.all(Value, C2)): (e, comps) =>
      given CSeq = comps
      val (v, c) = (<<[Value], <<[C2])
      v isA Value shouldBe true
      c isA C2 shouldBe true
      an[IllegalArgumentException] shouldBe thrownBy(comps.get[C1])
      sum += v.x
    sum shouldBe (v1.x + v2.x)

  it should "correctly iterate over the selected entities and update their component values" in:
    val am = ArchetypeManager()
    val toAdd = Map(
      Entity(0) -> CSeq(Value(1), C2()),
      Entity(1) -> CSeq(Value(2), C1(), C2())
    )
    for (entity, comps) <- toAdd do am.addEntity(entity, comps)
    val editedValue = Value(3)
    var sum = 0
    am.iterateWriting(archetype.all(Value)): (_, _) =>
      CSeq(editedValue)
    am.iterateReading(archetype.all(Value)): (_, comps) =>
      given CSeq = comps
      sum += <<[Value].x
    sum shouldBe editedValue.x * toAdd.size

  it should "not add the same entity multiple times" in:
    val am = ArchetypeManager()
    val entity = Entity(0)
    am.addEntity(entity, CSeq(C1()))
    an[IllegalArgumentException] shouldBe thrownBy(am.addEntity(entity, CSeq(C1())))

  it should "add components to an existing entity" in:
    val am = ArchetypeManager()
    val entity = Entity(0)
    am.addEntity(entity, CSeq(C1(), C2()))
    am.addComponents(entity, CSeq(Value(1)))
    am.iterateReading(archetype.all(Value)): (e, comps) =>
      given CSeq = comps
      val v = <<[Value]
      v isA Value shouldBe true
      val _ = v.x shouldBe 1

  it should "not add the same component to an entity that already has it" in:
    val am = ArchetypeManager()
    val entity = Entity(0)
    am.addEntity(entity, CSeq(C1()))
    an[IllegalArgumentException] shouldBe thrownBy(am.addComponents(entity, CSeq(C1())))

  it should "remove components from an existing entity" in:
    val am = ArchetypeManager()
    val entity = Entity(0)
    am.addEntity(entity, CSeq(C1(), C2(), Value(0)))
    am.removeComponents(entity, C1, Value)
    am.iterateReading(any(C1, Value)): (_, _) =>
      throw IllegalStateException("Unexpected")

  it should "not remove non-existing components from an entity" in:
    val am = ArchetypeManager()
    val entity = Entity(0)
    am.addEntity(entity, CSeq(C1()))
    an[IllegalArgumentException] shouldBe thrownBy(am.removeComponents(entity, C2))

  it should "correctly delete existing entities" in:
    val am = ArchetypeManager()
    val entity = Entity(0)
    am.addEntity(entity, CSeq(C1()))
    am.delete(entity)
    am.iterateReading(archetype.all(C1)): (_, _) =>
      throw IllegalStateException("Unexpected")
