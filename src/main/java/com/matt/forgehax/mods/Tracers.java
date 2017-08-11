package com.matt.forgehax.mods;

import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.SurfaceUtils;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.math.AngleHelper;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

/**
 * Created on 8/6/2017 by fr1kin
 */
@RegisterMod
public class Tracers extends ToggleMod {
    public Tracers() {
        super("Tracers", false, "See where other players are");
    }

    @SubscribeEvent
    public void onDrawScreen(RenderGameOverlayEvent.Text event) {
        final double center = 0.D;
        final double cx = MC.displayWidth / 4.f;
        final double cy = MC.displayHeight / 4.f;
        getWorld().loadedEntityList.stream()
                .filter(entity -> !Objects.equals(entity, getLocalPlayer()))
                .filter(entity -> entity instanceof EntityLiving)
                .forEach(entity -> {
                    Vec3d pos3d = EntityUtils.getInterpolatedEyePos(entity, MC.getRenderPartialTicks());
                    VectorUtils.ScreenPos pos = VectorUtils.toScreen(pos3d);
                    if(!pos.isVisible) {
                        // get position on ellipse

                        // dimensions of the ellipse
                        double dx = cx;
                        double dy = cy - 20;

                        // ellipse = x^2/a^2 + y^2/b^2 = 1
                        // e = (pos - C) / d
                        //  C = center vector
                        //  d = dimensions
                        double ex = (pos.x - cx) / dx;
                        double ey = (pos.y - cy) / dy;

                        // normalize
                        // n = u/|u|
                        double m = Math.abs(Math.sqrt(ex*ex + ey*ey));
                        double nx = ex / m;
                        double ny = ey / m;

                        // scale
                        // p = C + dot(n,d)
                        double x = cx + nx * dx;
                        double y = cy + ny * dy;

                        // --------------------
                        // now rotate triangle

                        // point - center
                        // w = <px - cx, py - cy>
                        double wx = x - cx;
                        double wy = y - cy;

                        // u = <w, h/2>
                        double ux = MC.displayWidth / 2.f;
                        double uy = center;

                        // |u|
                        double mu = Math.sqrt(ux*ux + uy*uy);
                        // |w|
                        double mw = Math.sqrt(wx*wx + wy*wy);

                        // theta = dot(u,w)/(|u|*|w|)
                        double ang = Math.toDegrees(Math.acos((ux*wx + uy*wy)/(mu*mw)));

                        // don't allow NaN angles
                        if(ang == Float.NaN) ang = 0;

                        // invert
                        if(y < cy) ang *= -1;

                        // normalize
                        ang = (float) AngleHelper.normalizeInDegrees(ang);

                        // --------------------

                        SurfaceUtils.drawTriangle((int) x, (int) y, 5, (float) ang, Utils.Colors.GREEN);
                    }
                });
    }
}