import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector4f
import java.io.File
import kotlin.experimental.and

class Md5AnimLoader {
    val hierarchy = mutableListOf<AnimJoint>()
    val baseframe = mutableListOf<Joint>()
    val dataframes = mutableListOf<MutableList<Float>>()
    val cache = mutableMapOf<Pair<Int,Int>,Joint>()
    private var sectionFunc = { x: String -> processTopLevel(x) }

    constructor(fname: String) {
        File(fname).forEachLine { line -> if(!line.isBlank())sectionFunc(line.trim().replace('\t',' ')) }
    }

    private fun processTopLevel(line: String) {
        if (line.isNullOrBlank()) {
            return
        }
        val tokens = line.split(" ")
        when (tokens[0]) {
            "hierarchy" -> sectionFunc = { s -> processHierarchy(s) }
            "baseframe" -> sectionFunc = { s -> processBaseframe(s) }
            "frame" -> {
                val frame = mutableListOf<Float>()
                dataframes.add(frame)
                sectionFunc = { s -> processDataframe(frame, s) }
            }
        }
    }

    private fun processHierarchy(line: String) {

        val tokens = line.split(" ")
        if (tokens[0].contains("}")) {
            sectionFunc = { x: String -> processTopLevel(x) }
        } else {
            hierarchy.add(AnimJoint(
                    name=tokens[0],
                    parentIndex = tokens[1].toInt(),
                    flag = tokens[2].toShort(),
                    startDataIndex = tokens[3].toInt()
            ))
        }
    }

    private fun processBaseframe(line: String) {

        val tokens = line.split(" ")
        if (tokens[0].contains("}")) {
            sectionFunc = { x: String -> processTopLevel(x) }
        } else {
            baseframe.add(Joint(
                    pos = Vector3f(
                            tokens[1].toFloat(),tokens[2].toFloat(),tokens[3].toFloat()
                    ),
                    orient = makeQuaternion(
                            tokens[6].toFloat(),tokens[7].toFloat(),tokens[8].toFloat()
                    ),
                    invMatrix = null
            ))
        }
    }


    private fun processDataframe(frame:MutableList<Float>, line: String) {
        val tokens = line.split(" ")
        if (tokens[0].contains("}")) {
            sectionFunc = { x: String -> processTopLevel(x) }
        } else {
            frame.addAll(tokens.map{ x->x.toFloat()})
        }
    }

    fun getJointForFrame(jointIndex:Int, frameIndex:Int): Joint {
        val key = Pair(jointIndex,frameIndex)
        val joint = hierarchy[jointIndex]
        return cache.getOrPut(key,{
            val frame = dataframes[frameIndex]
            val basejoint = baseframe[jointIndex]
            val calculatedParentJoint = if (joint.parentIndex >= 0) getJointForFrame(joint.parentIndex, frameIndex) else null
            joint.calculateJointForFrame(frame = frame, basejoint = basejoint, parent = calculatedParentJoint)
        })
    }

    fun getAllJointsForFrame(frameIndex: Int): List<Joint>{
        return hierarchy.withIndex().map{ (i, _)->getJointForFrame(i,frameIndex)}
    }

    fun getOrderedVerticesWithNormalsFromTris(mesh: Mesh, joints: List<Joint>, bindposeJoints: List<Joint>, frameIndex: Int): List<OutVert> {
        val frame = dataframes[frameIndex]
        val localJointMatrices = mutableListOf<Matrix4f>()
        val correctedJointMatrices = mutableListOf<Matrix4f>()
        for ((i, joint) in joints.withIndex()) {
            val animJoint = hierarchy[i]
            val basejoint = bindposeJoints[i]
            val baseframe = baseframe[i]

            var h = 0
            val pos = Vector3f(
                    if (animJoint.flag.and(1) > 0) frame[animJoint.startDataIndex + h++] else baseframe.pos.x,
                    if (animJoint.flag.and(2) > 0) frame[animJoint.startDataIndex + h++] else baseframe.pos.y,
                    if (animJoint.flag.and(4) > 0) frame[animJoint.startDataIndex + h++] else baseframe.pos.z
            )
            val orient = makeQuaternion(
                    if (animJoint.flag.and(8) > 0) frame[animJoint.startDataIndex + h++] else baseframe.orient.x,
                    if (animJoint.flag.and(16) > 0) frame[animJoint.startDataIndex + h++] else baseframe.orient.y,
                    if (animJoint.flag.and(32) > 0) frame[animJoint.startDataIndex + h++] else baseframe.orient.z
            )

            var jointMatrix = Matrix4f().translate(pos).rotate(orient)
            if (animJoint.parentIndex > -1) {
                jointMatrix = Matrix4f(localJointMatrices[animJoint.parentIndex]).mul(jointMatrix)
            }
            localJointMatrices.add(jointMatrix)
            correctedJointMatrices.add(Matrix4f(jointMatrix).mul(basejoint.invMatrix))

        }

        return mesh.tris.flatMap { tri ->
            tri.vertIndexes.map { i ->
                val vert = mesh.verts[i]
                val pos = Vector3f()
                val normal = Vector3f()
                for (i in vert.startWeight..(vert.startWeight + vert.countWeights - 1)) {
                    val weight = mesh.weights[i]
                    val jointMatrix = correctedJointMatrices[weight.jointIndex]
                    val tmpPos = Vector4f(vert.bindposePosition, 1.0f).mul(jointMatrix).mul(weight.bias)
                    val tmpNormal = Vector4f(vert.bindposeNormal, 0.0f).mul(jointMatrix).mul(weight.bias)
                    pos.add(tmpPos.x, tmpPos.y, tmpPos.z)
                    normal.add(tmpNormal.x, tmpNormal.y, tmpNormal.z)
                }
                OutVert(
                        pos = pos,
                        normal = normal
                )
            }
        }
    }

    fun getAllJointsForInterpolatedFrame(frameIndex: Int): List<Joint>{
        return hierarchy.withIndex().map{ (i, _)->getJointForFrame(i,frameIndex)}
    }

}

data class AnimJoint (val name:String, val parentIndex:Int, val flag:Short, val startDataIndex:Int){
    fun projectDataframeWithFlags(frame:List<Float>, basejoint: Joint):Joint  {
        var i = 0
        val pos = Vector3f(
                if(flag.and(1)>0) frame[startDataIndex+i++] else basejoint.pos.x,
                if(flag.and(2)>0) frame[startDataIndex+i++] else basejoint.pos.y,
                if(flag.and(4)>0) frame[startDataIndex+i++] else basejoint.pos.z
        )
        val orient = makeQuaternion(
                if(flag.and(8)>0) frame[startDataIndex+i++] else basejoint.orient.x,
                if(flag.and(16)>0) frame[startDataIndex+i++] else basejoint.orient.y,
                if(flag.and(32)>0) frame[startDataIndex+i++] else basejoint.orient.z
        )
        return Joint(pos = pos, orient = orient, invMatrix = null)
    }

    fun calculateJointForFrame(frame:List<Float>, basejoint: Joint, parent:Joint?): Joint {

        val joint = projectDataframeWithFlags(frame, basejoint).copy()
        if(parent != null){
            joint.pos.rotate(parent.orient)
            joint.pos.add(parent.pos)
//            joint.orient.mul(parent.orient)
            val newOrient = Quaternionf(parent.orient)
            newOrient.mul(joint.orient)
            val response = Joint(pos = joint.pos, orient = newOrient.normalize(), invMatrix = null)
            return response
        }else{
            val response = Joint(pos = joint.pos, orient = Quaternionf(-0.5f, -0.5f, -0.5f, -0.5f), invMatrix = null)
            return response
        }

    }
}

