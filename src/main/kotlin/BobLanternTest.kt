fun main(args:Array<String>){
    val bob = Md5Loader("bob_lamp.md5mesh")
    for (mesh in bob.meshes){
        println("Mesh ${mesh.shader} verts:${mesh.numverts} ")
        println("Verts: ${mesh.getOrderedVerticesFromTris(bob.bindposeJoints)}")
    }
    val anim = Md5AnimLoader("bob_lamp.md5anim")
    for((i,joint) in anim.hierarchy.withIndex()){
        println("AnimJoint ${i} flags:${joint.flag}\t startIndex: ${joint.startDataIndex} name: ${joint.name} parent:${anim.hierarchy.getOrNull(joint.parentIndex)?.name}")
    }

}