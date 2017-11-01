import org.joml.Vector2f
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JCheckBox
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.Timer


class Gui(gamestate: GameState) : JFrame() {
    private var gamestate: GameState

    init {
        this.gamestate = gamestate
        initUI()
    }
    class MyMouseListener(val gamestate : GameState, val offset: Vector2f, val scale: Float) : MouseListener{
        override fun mouseReleased(e: MouseEvent?) {
        }

        override fun mouseEntered(e: MouseEvent?) {
        }

        override fun mouseExited(e: MouseEvent?) {}

        override fun mousePressed(e: MouseEvent?) {
            if(e!=null) {
                this.gamestate.camPosition.x = ((e.x.toFloat() + offset.x)/scale)
                this.gamestate.camPosition.z = ((e.y.toFloat() + offset.y)/scale)

            }
        }

        override fun mouseClicked(e: MouseEvent?) {
        }

    }
    class Radar(val gamestate: GameState, val offset: Vector2f, val scale: Float = 5f) : JPanel(), ActionListener {
        override fun actionPerformed(e: ActionEvent?) {
            repaint()
        }

        val timer = Timer(500,this)

        init {
            timer.start()
            this.addMouseListener(MyMouseListener(gamestate,offset, scale))
            this.preferredSize = Dimension(200,200)
            this.background = Color.PINK
        }

        @Override
        override fun paintComponent(g: Graphics?) {
            super.paintComponent(g)
            if(g==null){return}
            val g = g.create()
            val x = this.gamestate.camPosition.x
            val y = this.gamestate.camPosition.z
            g.drawOval(
                ((x*this.scale)-this.offset.x-(scale/2)).toInt(),
                ((y*this.scale)-this.offset.y-(scale/2)).toInt(),
                this.scale.toInt(),
                this.scale.toInt()
            )
            g.drawString("${x} ${y}",5,15)
        }
    }
    private fun initUI() {

        title = "Simple example"
        setSize(300, 200)
        setLocationRelativeTo(null)
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        this.setLayout(java.awt.FlowLayout())
        val checkboxIsRunning = JCheckBox("IsRunning")
        checkboxIsRunning.addActionListener(ActionListener { event -> this.gamestate.isRunning = !this.gamestate.isRunning })
        checkboxIsRunning.isSelected = this.gamestate.isRunning
        this.add(checkboxIsRunning)
        val radar = Radar(this.gamestate, Vector2f(-24f,0f), 24f)
        this.add(radar)
    }

}
