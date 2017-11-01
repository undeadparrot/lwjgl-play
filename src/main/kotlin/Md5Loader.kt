import org.joml.Quaternionf
import org.joml.Vector3f
import java.io.File

class Md5Loader {
    val meshes = mutableListOf<Mesh>()
    val joints = mutableListOf<Joint>()
    private var sectionFunc = { x: String -> processTopLevel(x) }

    constructor(fname: String) {
        val reader = File(fname).forEachLine { line -> sectionFunc(line.trim().replace('\t',' ')) }
    }

    private fun processTopLevel(line: String) {
        if (line.isNullOrBlank()) {
            return
        }
        val tokens = line.split(" ")
        when (tokens[0]) {
            "mesh" -> {
                val mesh = Mesh()
                meshes.add(mesh)
                sectionFunc = { s -> processMesh(mesh, s) }
            }
            "joints" -> sectionFunc = { s -> processJoints(s) }
        }
    }

    private fun processMesh(mesh: Mesh, line: String): Unit {
        if (line.isNullOrBlank()) {
            return
        }
        val tokens = line.split(" ")
        when (tokens[0]) {
            "}" -> sectionFunc = { x: String -> processTopLevel(x) }
            "shader" -> mesh.shader = tokens[1]
            "numverts" -> mesh.numverts = tokens[1].toInt()
            "numtris" -> mesh.numtris = tokens[1].toInt()
            "numweights" -> mesh.numweights = tokens[1].toInt()
            "vert" -> mesh.verts.add(Vert(
                    startWeight = tokens[6].toInt(),
                    countWeights = tokens[7].toInt()
            ))
            "tri" -> mesh.tris.add(Tri(
                    vertIndexes = arrayOf(tokens[2].toInt(), tokens[3].toInt(), tokens[4].toInt())
            ))
            "weight" -> mesh.weights.add(Weight(
                    jointIndex = tokens[2].toInt(),
                    bias = tokens[3].toFloat(),
                    pos = Vector3f(tokens[5].toFloat(), tokens[6].toFloat(), tokens[7].toFloat())
            ))
        }
    }

    private fun processJoints(line: String): Unit {
        if (line.isNullOrBlank()) {
            return
        }
        val tokens = line.split(" ")
        if (tokens[0].contains("}")) {
            sectionFunc = { x: String -> processTopLevel(x) }
        } else {

            joints.add(Joint(
                    name = tokens[0],
                    parent = tokens[1],
                    pos = Vector3f(tokens[3].toFloat(), tokens[4].toFloat(), tokens[5].toFloat()),
                    orient = makeQuaternion(tokens[8].toFloat(), tokens[9].toFloat(), tokens[10].toFloat())
            ))
        }
    }
    private fun processNormals(){
        for (mesh in meshes){
            for (tri in mesh.tris){

            }
        }
    }
}

data class Vert(val startWeight: Int, val countWeights: Int) {}
data class Weight(val jointIndex: Int, val bias: Float, val pos: Vector3f) {}
data class Tri(val vertIndexes: Array<Int>) {}
data class Joint(val name: String = "unknown", val parent: String = "unknown", val pos: Vector3f, val orient: Quaternionf) {}

class Mesh {
    var shader = "Unknown"
    var numverts = 0
    var numtris = 0
    var numweights = 0
    val verts = mutableListOf<Vert>()
    val tris = mutableListOf<Tri>()
    val weights = mutableListOf<Weight>()
    inline fun getVertexPosition(joints:List<Joint>,vertIndex:Int):Vector3f{
        val vert = verts[vertIndex]
        val pos = Vector3f()
        for(i in vert.startWeight.rangeTo(vert.startWeight+vert.countWeights-1)){
            val joint = joints[weights[i].jointIndex]
            val transformedWeightPos =Vector3f(weights[i].pos).rotate(joint.orient)
            val combinedPos = Vector3f(joint.pos)
                    .add(transformedWeightPos)
            val biasedPos = (Vector3f(combinedPos).mul(weights[i].bias))
            pos.add(biasedPos)
        }
        return pos
    }
    fun getOrderedVerticesFromTris(joints: List<Joint> ):List<Vector3f>{
        return tris.flatMap { tri -> tri.vertIndexes.map { i -> getVertexPosition(joints,i)} }
    }
    fun getTriNormals(joints: List<Joint>): List<Vector3f> {
        return tris.map{ tri ->
            Vector3f(getVertexPosition(joints, tri.vertIndexes[2]))
            .sub(getVertexPosition(joints,tri.vertIndexes[0]))
            .cross(
                    Vector3f(getVertexPosition(joints,tri.vertIndexes[1]))
                    .sub(getVertexPosition(joints,tri.vertIndexes[0]))
            )
        }
    }

}
fun makeQuaternion(x: Float, y: Float, z: Float): Quaternionf {
    val t = 1.0 - (x * x) - (y * y) - (z * z)
    val w = (if (t < 0.0) {
        0.0
    } else {
        Math.sqrt(t)*-1
    }).toFloat()
    return Quaternionf(x, y, z,w)
}
