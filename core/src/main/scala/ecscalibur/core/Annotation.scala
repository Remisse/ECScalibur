package ecscalibur.core

import ecscalibur.util.tpe

export annotations.*

object annotations:
  import scala.annotation.MacroAnnotation
  import scala.quoted.*
  import ecscalibur.core

  import tpe.getId

  /** Assigns a unique [[ComponentId]] to a component class. Will error out if it is used to
    * annotate something other than a class or if the annotated class does not extend the
    * [[Component]] trait and its companion object does not extend [[ComponentType]].
    */
  final class component extends MacroAnnotation:
    override def transform(using Quotes)(
        definition: quotes.reflect.Definition,
        companion: Option[quotes.reflect.Definition]
    ): List[quotes.reflect.Definition] =
      import quotes.reflect.*

      companion match
        case None => report.errorAndAbort("This class should define a companion object.")
        case _    => ()

      if definition.symbol.declaredTypes.nonEmpty then
        report.error(
          "Generic component classes are not allowed. Remove all type parameters from the class's definition."
        )

      val newRhs = Literal(IntConstant(getId(definition.symbol.fullName)))

      def ensureExtends[T](cls: Symbol)(using Quotes, Type[T]): Unit =
        cls.typeRef.asType match
          case '[T] => ()
          case _    => report.error(s"${cls.toString} must extend ${TypeRepr.of[T].show}.")

      def recreateField(fieldName: String, cls: Symbol, rhs: Term)(using Quotes): ValDef =
        // Works as long as this field is non-private (even protected is fine). If the field
        // is private, compilation will fail silently.
        val idSym = cls.fieldMember(fieldName)
        val idOverrideSym =
          Symbol.newVal(
            cls,
            idSym.name,
            idSym.info,
            Flags.Override | Flags.Protected,
            Symbol.noSymbol
          )
        ValDef(idOverrideSym, Some(rhs))

      def newClassDefinitionWithOverriddenField[typeToExtend](toCopy: Definition)(using
          Type[typeToExtend]
      ): ClassDef =
        toCopy match
          case ClassDef(name, ctr, parents, selfOpt, body) =>
            val cls = toCopy.symbol
            ensureExtends[typeToExtend](cls)
            ClassDef.copy(toCopy)(
              name,
              ctr,
              parents,
              selfOpt,
              recreateField("_typeId", cls, newRhs) :: body
            )
          case _ => report.errorAndAbort("This annotation only works on classes.")

      List(
        newClassDefinitionWithOverriddenField[Component](definition),
        newClassDefinitionWithOverriddenField[ComponentType](companion.head)
      )
