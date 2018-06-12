package SupplySugarMachine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SupplySugar_Render extends TileEntitySpecialRenderer {
	public static SupplySugar_Render renderer;
	ResourceLocation texture = null;
	Minecraft mc = null;
	double updown = 0.0;
	boolean isUp = true;
	double rot = 0.0;

	public SupplySugar_Render() {
		texture = new ResourceLocation("supplysugar","textures/marker/supplysugarmachine_marker.png");
		mc = Minecraft.getMinecraft();
	}

    public void setTileEntityRenderer(TileEntityRendererDispatcher par1TileEntityRenderer)
    {
        super.func_147497_a(par1TileEntityRenderer);
        renderer = this;
    }

	public void renderText(TileEntity tile, double coordX, double coordY, double coordZ, float scale) {
		if (!((TileEntitySupplySugar)tile).CustomName.isEmpty()) {
			//EntityPlayer player = tile.getWorldObj().getPlayerEntityByName(mc.thePlayer..getDisplayName());
			GL11.glPushMatrix();
			GL11.glTranslatef((float) coordX + 0.5F, (float) coordY + 1.2F, (float) coordZ + 0.5F);
			GL11.glRotated(-mc.thePlayer.rotationYawHead, 0, 1, 0);
			GL11.glRotated(mc.thePlayer.rotationPitch, 1, 0, 0);
			GL11.glScalef(scale, scale, scale);
			FontRenderer fontrenderer = this.func_147498_b();
			byte b0 = 0;
			String signText = ((TileEntitySupplySugar)tile).CustomName;
			fontrenderer.drawString(signText, -fontrenderer.getStringWidth(signText) / 2, -5, b0);
			GL11.glPopMatrix();
		}
	}

	@Override
	public void renderTileEntityAt(TileEntity tile, double x,
			double y, double z, float f) {
		// TODO 自動生成されたメソッド・スタブ
		renderText(tile, x, y, z, -0.05F);
		//tile.getWorldObj().getPlayerEntityByName("aoyanagiYuu").addChatMessage(new ChatComponentText("Sugar: "+((TileEntitySupplySugar)tile).SugarNumber));
		if (((TileEntitySupplySugar)tile).getSugarSize() == 0) {
			Tessellator tessellator = Tessellator.instance;
			final float FACE_XZ_NORMAL = 0.8944F;
			final float FACE_Y_NORMAL  = 0.4472F;

			GL11.glPushMatrix();
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTranslated(x + 0.5D, y + 1.5D + updown, z + 0.5D);
			GL11.glColor4f(1.0F, 0.0F, 0.0F, 0.5F);
			GL11.glRotated(rot, 0, 1, 0);
			this.bindTexture(texture);
			//tessellator.setColorRGBA_F(1.0F, 0.0F, 0.0F, 0.5F);
			//tessellator.setColorOpaque_F(0.5F, 0.5F, 0.5F);
			tessellator.startDrawing(GL11.GL_TRIANGLES);
			tessellator.setNormal(0.0F, FACE_Y_NORMAL, FACE_XZ_NORMAL);
			tessellator.addVertex(0.5D, 1.0D, 0.5D);
			tessellator.addVertex(-0.5D, 1.0D, 0.5D);
			tessellator.addVertex(0.0D, 0.0D, 0.0D);
			tessellator.draw();

			tessellator.startDrawing(GL11.GL_TRIANGLES);
			tessellator.setNormal(-FACE_XZ_NORMAL, FACE_Y_NORMAL, 0.0F);
			tessellator.addVertex(-0.5D, 1.0D, 0.5D);
			tessellator.addVertex(-0.5D, 1.0D, -0.5D);
			tessellator.addVertex(0.0D, 0.0D, 0.0D);
			tessellator.draw();

			tessellator.startDrawing(GL11.GL_TRIANGLES);
			tessellator.setNormal(0.0F, FACE_Y_NORMAL, -FACE_XZ_NORMAL);
			tessellator.addVertex(-0.5D, 1.0D, -0.5D);
			tessellator.addVertex(0.5D, 1.0D, -0.5D);
			tessellator.addVertex(0.0D, 0.0D, 0.0D);
			tessellator.draw();

			tessellator.startDrawing(GL11.GL_TRIANGLES);
			tessellator.setNormal(FACE_XZ_NORMAL, FACE_Y_NORMAL, 0.0F);
			tessellator.addVertex(0.5D, 1.0D, -0.5D);
			tessellator.addVertex(0.5D, 1.0D, 0.5D);
			tessellator.addVertex(0.0D, 0.0D, 0.0D);
			tessellator.draw();

			tessellator.startDrawing(GL11.GL_QUADS);
			tessellator.setNormal(0.0F, 1.0F, 0.0F);
			tessellator.addVertex(0.5D, 1.0D, -0.5D);
			tessellator.addVertex(-0.5D, 1.0D, -0.5D);
			tessellator.addVertex(-0.5D, 1.0D, 0.5D);
			tessellator.addVertex(0.5D, 1.0D, 0.5D);
			tessellator.draw();

			GL11.glPopMatrix();

			rot += 1.0;
			if (updown > 0.25) {
				isUp = false;
			}
			else if (updown < -0.25) {
				isUp = true;
			}
			if (isUp) {
				updown += 0.005;
			}
			else {
				updown -= 0.005;
			}
		}
	}
}
