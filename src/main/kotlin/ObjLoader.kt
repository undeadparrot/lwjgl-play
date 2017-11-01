import java.io.File

class ObjLoader {
    val vertices  = mutableListOf<Float>()
    val indices = mutableListOf<Int>()
    constructor( fname: String){
        File(fname).useLines { lines->lines.forEach{
            line-> processLine(line)
        } }

    }
    private fun processLine(line: String){
        if(line.isNullOrBlank()){return}
        val tokens = line.split(" ")
        when(tokens[0]){
            "v" -> {
                vertices.addAll(tokens.slice(1..3).map{x->x.toFloat()})
            }
            "f" -> {
                indices.addAll(
                        tokens.slice(1..3).map{
                            triple->triple.split("/")[0].toInt()-1
                        }
                )
            }
        }
    }
    public fun getOrderedVerts():List<Float>{
        return indices.flatMap{i-> floatArrayOf(
                vertices[(i*3)],
                vertices[(i*3)+1],
                vertices[(i*3)+2]
        ).asIterable()}
    }
}