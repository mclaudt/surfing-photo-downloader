import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.JpegWriter

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


object Run extends App{

  //required photo id
  val id = 27537

  //Max possible zoom is 15, for 16 all goes wrong
  val zoom = 15

  val l = zoom*2-1

  val widthSource = 400

  val heightSource = 266

  val dx = widthSource/(2.0*zoom)

  val dy = heightSource/(2.0*zoom)

  val widthPiece = 200

  val heightPiece = 133

  val result= (for {
    i<- 1 to l
    j<- 1 to l
  } yield (i,j)).map(t => (t, getImage(id, t._1, t._2))).foldLeft(Image.filled(l * widthPiece, l * heightPiece))(
    (img, pair) =>img.overlay(pair._2, (pair._1._1-1) * widthPiece, (pair._1._2-1) * heightPiece)).output(new File(s"surf_${id}.jpg"))


  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  def getImage(id:Int, x1:Int, y1:Int): Image =  {

    val resF = Http().singleRequest(HttpRequest(uri =url(id,x1,y1)))
        .flatMap(
        _.entity.toStrict(10.seconds)).map(
      e=>e.data.toArray
    ).map(a=>Image(a).trimLeft(widthPiece).trimBottom(heightPiece))
   val res = Await.ready(resF, Duration.Inf).value.get.get
    println(s"$x1, $y1 ok")
    res
  }

  implicit val writer = JpegWriter()

  private def url(id:Int,x1:Int,y1:Int) =
      "http://media.surfingsilverc0ast.com/members/image.php?" +
        s"id=${id}&" +
        s"x0=0&" +
        s"y0=0&" +
        s"x1=${f"${(x1 - 1) * dx}%1.3f"}&" +
        s"y1=${f"${(y1 - 1) * dy}%1.3f"}&" +
        s"z=${zoom}&" +
        s"width=${2*widthPiece}&" +
        s"height=${2*heightPiece}"


}
