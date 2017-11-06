import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector4f
import java.io.File

class Md5Loader {
    val meshes = mutableListOf<Mesh>()
    val bindposeJoints = mutableListOf<Joint>()
    private var sectionFunc = { x: String -> processTopLevel(x) }

    constructor(fname: String) {
        val reader = File(fname).forEachLine { line -> sectionFunc(line.trim().replace('\t',' ')) }
        meshes.forEach {
            it.calculateTriangleNormals(bindposeJoints)
            it.calculateBindposeNormals(bindposeJoints)
            it.calculateBindposePositions(bindposeJoints)
        }
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
            val pos = Vector3f(tokens[3].toFloat(), tokens[4].toFloat(), tokens[5].toFloat())
            val orient = makeQuaternion(tokens[8].toFloat(), tokens[9].toFloat(), tokens[10].toFloat())
            bindposeJoints.add(Joint(
                    name = tokens[0],
                    parent = tokens[1],
                    pos = pos,
                    orient = orient,
                    invMatrix = Matrix4f().translate(pos).rotate(orient).invert()
            ))
        }
    }
}

data class Vert(val startWeight: Int, val countWeights: Int, var bindposeNormal: Vector3f = Vector3f(), var bindposePosition: Vector3f = Vector3f())
data class OutVert(val pos: Vector3f, val normal: Vector3f)
data class Weight(val jointIndex: Int, val bias: Float, val pos: Vector3f, val normal: Vector3f = Vector3f())
data class Tri(val vertIndexes: Array<Int>) {
    var centroid: Vector3f = Vector3f()
    var normal: Vector3f = Vector3f()
}

data class Joint(val name: String = "unknown", val parent: String = "unknown", val pos: Vector3f, val orient: Quaternionf, val invMatrix: Matrix4f?)

class Mesh {
    var shader = "Unknown"
    var numverts = 0
    var numtris = 0
    var numweights = 0
    val verts = mutableListOf<Vert>()
    val tris = mutableListOf<Tri>()
    val weights = mutableListOf<Weight>()
    inline fun getVertexPosition(joints:List<Joint>, vertIndex:Int):Vector3f{
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

    inline fun getVertexNormal(joints: List<Joint>, vertIndex: Int, bindposeJoints: List<Joint>): Vector3f {
        val vert = verts[vertIndex]
        val normal = Vector3f(0f, 0f, 0f)
        for (i in vert.startWeight.rangeTo(vert.startWeight + vert.countWeights - 1)) {
            val weight = weights[i]
            val joint = joints[weight.jointIndex]
            val bpInvMatrix = bindposeJoints[weight.jointIndex].invMatrix
            val jointMatrix = Matrix4f().translate(joint.pos).rotate(joint.orient).mul(bpInvMatrix)
            val transformedNormal = Vector4f(vert.bindposeNormal, 0.0f).mul(jointMatrix).normalize().mul(weight.bias)
            normal.add(transformedNormal.x, transformedNormal.y, transformedNormal.z)
        }
        return normal.normalize()
    }

    fun getOrderedVerticesFromTris(joints: List<Joint> ):List<Vector3f>{
        return tris.flatMap { tri -> tri.vertIndexes.map { i -> getVertexPosition(joints,i)} }
    }

    fun calculateTriangleNormals(bindposeJoints: List<Joint>) {
        tris.forEach {
            val v1 = getVertexPosition(bindposeJoints, it.vertIndexes[0])
            val v2 = getVertexPosition(bindposeJoints, it.vertIndexes[1])
            val v3 = getVertexPosition(bindposeJoints, it.vertIndexes[2])
            //calculate centre by averaging x,y,z coords
            it.centroid = Vector3f(v1).add(v2).add(v3).div(3f)
            //calculate normals
            it.normal = Vector3f(v2).sub(v1).cross(Vector3f(v3).sub(v1)).normalize()
        }
    }

    fun calculateBindposeNormals(bindposeJoints: List<Joint>) {
        val vertTriNormals = mutableMapOf<Int, MutableList<Vector3f>>()
        tris.forEach {
            with(vertTriNormals) {
                getOrPut(it.vertIndexes[0], { mutableListOf<Vector3f>() }).add(it.normal)
                getOrPut(it.vertIndexes[1], { mutableListOf<Vector3f>() }).add(it.normal)
                getOrPut(it.vertIndexes[2], { mutableListOf<Vector3f>() }).add(it.normal)
            }
        }
        vertTriNormals.forEach { vertIndex, normals ->
            verts[vertIndex].bindposeNormal =
                    normals.reduce(Vector3f::add)
                            .div(normals.size.toFloat())
                            .normalize()

        }
    }

    fun calculateBindposePositions(bindposeJoints: List<Joint>) {
        verts.withIndex().forEach { (vertIndex, vert) ->
            vert.bindposePosition =
                    getVertexPosition(bindposeJoints, vertIndex)

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
