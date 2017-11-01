import org.joml.Quaternionf
import org.joml.Vector3f
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
                    )
            ))
        }
    }


    private fun processDataframe(frame:MutableList<Float>,line: String) {
         val tokens = line.split(" ")
        if (tokens[0].contains("}")) {
            sectionFunc = { x: String -> processTopLevel(x) }
        } else {
            frame.addAll(tokens.map{x->x.toFloat()})
        }
    }

    fun getJointForFrame(jointIndex:Int,frameIndex:Int): Joint {
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
        return hierarchy.withIndex().map{(i, _)->getJointForFrame(i,frameIndex)}
    }


    fun getAllJointsForInterpolatedFrame(frameIndex: Int): List<Joint>{
        return hierarchy.withIndex().map{(i, _)->getJointForFrame(i,frameIndex)}
    }

    fun getDebugVertices(frameIndex: Int ):List<Float>{
        return hierarchy.withIndex().flatMap{(i, animjoint)->
            val joint = getJointForFrame(i,frameIndex)
            if(animjoint.parentIndex<0){
                listOf(joint.pos.x,joint.pos.y,joint.pos.z,joint.pos.x,joint.pos.y,joint.pos.z)
            }else {
                val parent = getJointForFrame(animjoint.parentIndex, 0)
                listOf(parent.pos.x, parent.pos.y, parent.pos.z, joint.pos.x, joint.pos.y, joint.pos.z)
            }
        }
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
        return Joint(pos =pos, orient = orient)
    }
    fun calculateJointForFrame(frame:List<Float>, basejoint: Joint, parent:Joint?): Joint {

        val joint = projectDataframeWithFlags(frame, basejoint).copy()
        if(parent != null){
            joint.pos.rotate(parent.orient)
            joint.pos.add(parent.pos)
//            joint.orient.mul(parent.orient)
            val newOrient = Quaternionf(parent.orient)
            newOrient.mul(joint.orient)
            val response = Joint(pos =joint.pos, orient =newOrient.normalize())
            return response
        }else{
            val response = Joint(pos =joint.pos, orient=Quaternionf(-0.5f,-0.5f,-0.5f, -0.5f))
            return response
        }

    }
}

