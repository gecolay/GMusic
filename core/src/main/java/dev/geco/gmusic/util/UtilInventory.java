package dev.geco.gmusic.util;

import org.bukkit.inventory.*;

public class UtilInventory {
	
	public int getPlayerInventorySpace(PlayerInventory P, ItemStack S) {
		int am = 0;
		for(ItemStack i : P.getContents()) {
			if(i != null && S.clone().isSimilar(i.clone()) && i.getAmount() < S.getMaxStackSize()) am += (S.getMaxStackSize() - i.getAmount());
			else if(i == null) am += S.getMaxStackSize();
		}
		for(ItemStack i : P.getArmorContents()) {
			if(i != null && S.clone().isSimilar(i.clone()) && i.getAmount() < S.getMaxStackSize()) am -= (S.getMaxStackSize() - i.getAmount());
			else if(i == null) am -= S.getMaxStackSize();
		}
		return am;
	}
	
	public void removePlayerInventoryItem(PlayerInventory P, ItemStack S, long A) {
		int a  = P.getContents().length - 6;
		for(int i = 0; i <= a; i++) {
			ItemStack r = P.getItem(i);
			if(r != null && S.clone().isSimilar(r.clone())) {
				A -= r.getAmount();
				if(A < 0) {
					r.setAmount((int) (A * -1));
					P.setItem(i, r);
					break;
				} else {
					r.setAmount(0);
					P.setItem(i, r);
				}
			}
		}
		if(A > 0) {
			int z = P.getItemInOffHand().getAmount() - (int) A;
			if(z >= 0) {
				S.setAmount(z);
				P.setItemInOffHand(S);
			}
		}
	}
	
	public void addPlayerInventoryItem(PlayerInventory P, ItemStack S, long A) {
		int a  = P.getContents().length - 6;
		for(int i = 0; i <= a; i++) {
			ItemStack r = P.getItem(i);
			if(A > 0 && r != null && S.clone().isSimilar(r.clone()) && S.getMaxStackSize() >= r.getAmount()) {
				if(S.getMaxStackSize() - r.getAmount() >= A) {
					r.setAmount((int) (r.getAmount() + A));
					A = 0;
					P.setItem(i, r);
				} else {
					A -= S.getMaxStackSize() - r.getAmount();
					r.setAmount(S.getMaxStackSize());
					P.setItem(i, r);
				}
			} else if(A > 0 && r == null) {
				if(S.getMaxStackSize() > A) {
					S.setAmount((int) A);
					A = 0;
					P.setItem(i, S);
				} else {
					A -= S.getMaxStackSize();
					S.setAmount(S.getMaxStackSize());
					P.setItem(i, S);
				}
			}
		}
		if(A > 0) {
			int z = P.getItemInOffHand().getAmount() + (int) A;
			S.setAmount(z <= S.getMaxStackSize() ? z : S.getMaxStackSize());
			P.setItemInOffHand(S);
		}
	}
	
}