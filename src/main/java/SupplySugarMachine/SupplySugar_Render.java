package SupplySugarMachine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class SupplySugar_Render extends TileEntitySpecialRenderer {
	ResourceLocation texture = null;
	Minecraft mc = null;
	double updown = 0.0;
	boolean isUp = true;
	double rot = 0.0;
	public SupplySugar_Render() {
		texture = new ResourceLocation("supplysugar","textures/marker/supplysugarmachine_marker.png");
		mc = Minecraft.getMinecraft();
	}
/*
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
		// TODO 自動生成されたメソッド・スタブ
		Tessellator tessellator = Tessellator.instance;
	    // if you don't perform this translation, the item won't sit in the player's hand properly in 3rd person view
	    GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

	    // for "inventory" blocks (actually for items which are equipped, dropped, or in inventory), should render in [0,0,0] to [1,1,1]
	    tessellator.startDrawingQuads();
	    renderPyramid(tessellator, 0.0, 0.0, 0.0);
	    tessellator.draw();

	    // don't forget to undo the translation you made at the start
	    GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {
		// TODO 自動生成されたメソッド・スタブ
			Tessellator tessellator = Tessellator.instance;
			// world blocks should render in [x,y,z] to [x+1, y+1, z+1]
		    //     tessellator.startDrawingQuads() has already been called by the caller

		    int lightValue = block.getMixedBrightnessForBlock(world, x, y, z);
		    tessellator.setBrightness(lightValue);
		    tessellator.setColorOpaque_F(1.0F, 5.0F, 1.0F);

		    renderPyramid(tessellator, (double)x, (double)y, (double) z);
		    //     tessellator.draw() will be called by the caller after return
			//renderer.setRenderBounds(0.2D, 3.2D, 0.2D, 0.8D, 0.8D, 0.8D);
			//GL11.glTranslatef(-0.5F, 3.5F, -0.5F);
			//renderer.renderStandardBlock(block, x, y, z);
			//renderer.setRenderBounds(0.2D, 3.2D, 0.2D, 0.8D, 0.8D, 0.8D);
			//renderer.renderStandardBlock(block, x, y, z);
			//renderer.setRenderBounds(0.2D, 3.2D, 0.2D, 0.8D, 0.8D, 0.8D);
			//renderer.renderStandardBlock(block, x, y, z);
			//renderer.setRenderBounds(0.2D, 3.2D, 0.2D, 0.8D, 0.8D, 0.8D);
			//renderer.renderStandardBlock(block, x, y, z);
			//GL11.glColor3d(1.0d, 0.0d, 0.0d);
		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		// TODO 自動生成されたメソッド・スタブ
		return true;
	}

	@Override
	public int getRenderId() {
		// TODO 自動生成されたメソッド・スタブ
		return LMM_LittleMaidMobX.SupplySugar_RenderID;
	}
*/
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
