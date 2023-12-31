import org.example.EnsureSerializable
import java.io.Serializable

@EnsureSerializable
data class GrandParent constructor(
    val a: String,
    val b: Parent
)

data class Parent(
    val a: Child1,
    val b: Child2,
    val c: Child3,
) : Serializable, Cloneable

class Child1 : Serializable
class Child2 : Serializable
class Child3(val a: Child3?) : Serializable