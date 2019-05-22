package mickworks.ui.rotate3dtransition

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.transition.TransitionValues
import android.transition.Visibility
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.view.animation.Interpolator
import kotlin.math.abs

class Rotate3DTransition(private var gravity: Int = Gravity.END): Visibility() {

    var DURATION = 1000L //Default
    var interpolator: Interpolator = BounceInterpolator() //Default
    var factor = 1.0f  //Default

    private var calculator = when(gravity){
        Gravity.END -> object : CalculatorHorizontal() {
            override fun getRotationY(sceneRoot: ViewGroup, view: View, factor: Float): Float = view.rotationY + 90f * factor
            override fun getTranslationX(sceneRoot: ViewGroup, view: View, factor: Float): Float {
                val isRtl = sceneRoot.layoutDirection == View.LAYOUT_DIRECTION_RTL
                return if (isRtl) view.translationX - sceneRoot.width * factor else view.translationX + sceneRoot.width * factor
            }
        }
        Gravity.START -> object : CalculatorHorizontal() {
            override fun getRotationY(sceneRoot: ViewGroup, view: View, factor: Float): Float = view.rotationY - 90f * factor
            override fun getTranslationX(sceneRoot: ViewGroup, view: View, factor: Float): Float {
                val isRtl = sceneRoot.layoutDirection == View.LAYOUT_DIRECTION_RTL
                return if (isRtl) view.translationX + sceneRoot.width * factor else view.translationX - sceneRoot.width * factor
            }
        }
        Gravity.BOTTOM -> object : CalculatorVertical(){
            override fun getRotationX(sceneRoot: ViewGroup, view: View, factor: Float): Float = view.rotationX - 90f * factor
            override fun getTranslationY(sceneRoot: ViewGroup, view: View, factor: Float): Float = view.translationY + sceneRoot.height * factor
        }
        Gravity.TOP -> object : CalculatorVertical(){
            override fun getRotationX(sceneRoot: ViewGroup, view: View, factor: Float): Float = view.rotationX + 90f * factor
            override fun getTranslationY(sceneRoot: ViewGroup, view: View, factor: Float): Float = view.translationY - sceneRoot.height * factor
        }
        else -> null
    }

    private fun captureValues(transitionValues: TransitionValues){
        val view = transitionValues.view
        val rotationY = view.rotationY
        val translationX = view.translationX
        transitionValues.values.apply {
            put(PROPNAME_ROTATION_Y, rotationY)
            put(PROPNAME_TRANSRATION_X, translationX)
        }
    }

    /**
     * @TODO
     */
    override fun getTransitionProperties(): Array<String> {
        return super.getTransitionProperties()//TRANSITION_PROPERTIES
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        super.captureStartValues(transitionValues)
        Log.d("$CLASS:captureStartValues", "${transitionValues.view},,,${transitionValues.values["android:visibility:visibility"]},,,${transitionValues.values["android:visibility:parent"]}")
        captureValues(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        super.captureEndValues(transitionValues)
        Log.d("$CLASS:captureEndValues", "${transitionValues.view},,,${transitionValues.values["android:visibility:visibility"]},,,${transitionValues.values["android:visibility:parent"]}")
        captureValues(transitionValues)
    }

    override fun onAppear(
        sceneRoot: ViewGroup?,
        view: View?,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        if (endValues == null) return null

        val width = sceneRoot?.width!!.toFloat()

        val endTX = view?.translationX!!
        val endRY = view.rotationY
        val startTX = calculator?.getTranslationX(sceneRoot, view, this.factor)!!
        val startRY = calculator?.getRotationY(sceneRoot, view, this.factor)!!

        val endTY = view.translationY
        val endRX = view.rotationX
        val startTY = calculator?.getTranslationY(sceneRoot, view, this.factor)!!
        val startRX = calculator?.getRotationX(sceneRoot, view, this.factor)!!

        Log.d("$CLASS:onAppear", "$startTX,,,$endTX,,,$startTY,,,$endTY")

        view.pivotX = when(calculator){
            is CalculatorHorizontal -> if (startTX < 0) abs(startTX) else 0f
            else -> (view.width/2).toFloat()
        }
        view.pivotY = when(calculator){
            is CalculatorVertical -> if (startTY < 0) abs(startTY) else 0f
            else -> (view.height/2).toFloat()
        }
        view.cameraDistance = width*15f

        val animRotationY = PropertyValuesHolder.ofFloat("rotationY", startRY, endRY)
        val animTranslationX = PropertyValuesHolder.ofFloat("translationX", startTX, endTX)
        val animRotationX = PropertyValuesHolder.ofFloat("rotationX", startRX, endRX)
        val animTranslationY = PropertyValuesHolder.ofFloat("translationY", startTY, endTY)


        val animator = ObjectAnimator.ofPropertyValuesHolder(view, animRotationY, animTranslationX, animRotationX, animTranslationY)
        animator.apply {
            interpolator = this@Rotate3DTransition.interpolator
            duration = DURATION
        }
        return animator
    }

    override fun onDisappear(
        sceneRoot: ViewGroup?,
        view: View?,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        if (startValues == null) return null

        val width = sceneRoot?.width!!.toFloat()

        val startTX = view?.translationX!!
        val startRY = view.rotationY
        val endTX = calculator?.getTranslationX(sceneRoot, view, this.factor)!!
        val endRY = calculator?.getRotationY(sceneRoot, view, this.factor)!!

        val startTY = view.translationY
        val startRX = view.rotationX
        val endTY = calculator?.getTranslationY(sceneRoot, view, this.factor)!!
        val endRX = calculator?.getRotationX(sceneRoot, view, this.factor)!!

        Log.d("$CLASS:onDisappear", "$startTX,,,$endTX,,,$startTY,,,$endTY")

        view.pivotX = when(calculator){
            is CalculatorHorizontal -> if (endTX < 0) abs(endTX) else 0f
            else -> (view.width/2).toFloat()
        }
        view.pivotY = when(calculator){
            is CalculatorVertical -> if (endTY < 0) abs(endTY) else 0f
            else -> (view.height/2).toFloat()
        }
        view.cameraDistance = width*15f

        val animRotationY = PropertyValuesHolder.ofFloat("rotationY", startRY, endRY)
        val animTranslationX = PropertyValuesHolder.ofFloat("translationX", startTX, endTX)
        val animRotationX = PropertyValuesHolder.ofFloat("rotationX", startRX, endRX)
        val animTranslationY = PropertyValuesHolder.ofFloat("translationY", startTY, endTY)

        val animator = ObjectAnimator.ofPropertyValuesHolder(view, animRotationY, animTranslationX, animRotationX, animTranslationY)
        animator.apply {
            interpolator = this@Rotate3DTransition.interpolator
            duration = DURATION
        }
        return animator
    }


    interface Calculator {
        fun getRotationY(sceneRoot: ViewGroup, view: View, factor: Float): Float
        fun getTranslationX(sceneRoot: ViewGroup, view: View, factor: Float): Float

        fun getRotationX(sceneRoot: ViewGroup, view: View, factor: Float): Float
        fun getTranslationY(sceneRoot: ViewGroup, view: View, factor: Float): Float
    }

    /**
     * 縦遷移
     */
    abstract class CalculatorVertical: Calculator{
        override fun getRotationY(sceneRoot: ViewGroup, view: View, factor: Float): Float = view.rotationY
        override fun getTranslationX(sceneRoot: ViewGroup, view: View, factor: Float): Float = view.translationX
    }

    /**
     * 横遷移
     */
    abstract class CalculatorHorizontal: Calculator{
        override fun getRotationX(sceneRoot: ViewGroup, view: View, factor: Float): Float = view.rotationX
        override fun getTranslationY(sceneRoot: ViewGroup, view: View, factor: Float): Float = view.translationY
    }


    companion object {
        private const val PROPNAME_ROTATION_Y = "mickworks.ui.rotate3dtransition:rotationY"
        private const val PROPNAME_TRANSRATION_X = "mickworks.ui.rotate3dtransition:translationX"
        private val TRANSITION_PROPERTIES = arrayOf(PROPNAME_ROTATION_Y, PROPNAME_TRANSRATION_X)
        private val CLASS = this::class.java.simpleName
    }
}