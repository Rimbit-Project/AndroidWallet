/*
 * Copyright 2013-2014 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.rimbit.android_wallet.ui;

import java.math.BigInteger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import android.view.View;
import com.rimbit.android_wallet.ExchangeRatesProvider.ExchangeRate;
import com.rimbit.android_wallet.ui.CurrencyAmountView.Listener;
import com.rimbit.android_wallet.util.WalletUtils;

/**
 * @author Andreas Schildbach
 */
public final class CurrencyCalculatorLink
{
	private final CurrencyAmountView RBTAmountView;
	private final CurrencyAmountView localAmountView;

	private Listener listener = null;
	private boolean enabled = true;
	private ExchangeRate exchangeRate = null;
	private boolean exchangeDirection = true;

	private final CurrencyAmountView.Listener RBTAmountViewListener = new CurrencyAmountView.Listener()
	{
		@Override
		public void changed()
		{
			if (RBTAmountView.getAmount() != null)
				setExchangeDirection(true);
			else
				localAmountView.setHint(null);

			if (listener != null)
				listener.changed();
		}

		@Override
		public void focusChanged(final boolean hasFocus)
		{
			if (listener != null)
				listener.focusChanged(hasFocus);
		}
	};

	private final CurrencyAmountView.Listener localAmountViewListener = new CurrencyAmountView.Listener()
	{
		@Override
		public void changed()
		{
			if (localAmountView.getAmount() != null)
				setExchangeDirection(false);
			else
				RBTAmountView.setHint(null);

			if (listener != null)
				listener.changed();
		}

		@Override
		public void focusChanged(final boolean hasFocus)
		{
			if (listener != null)
				listener.focusChanged(hasFocus);
		}
	};

	public CurrencyCalculatorLink(@Nonnull final CurrencyAmountView RBTAmountView, @Nonnull final CurrencyAmountView localAmountView)
	{
		this.RBTAmountView = RBTAmountView;
		this.RBTAmountView.setListener(RBTAmountViewListener);

		this.localAmountView = localAmountView;
		this.localAmountView.setListener(localAmountViewListener);

		update();
	}

	public void setListener(@Nullable final Listener listener)
	{
		this.listener = listener;
	}

	public void setEnabled(final boolean enabled)
	{
		this.enabled = enabled;

		update();
	}

	public void setExchangeRate(@Nonnull final ExchangeRate exchangeRate)
	{
		this.exchangeRate = exchangeRate;

		update();
	}

	@CheckForNull
	public BigInteger getAmount()
	{
		if (exchangeDirection)
		{
			return RBTAmountView.getAmount();
		}
		else if (exchangeRate != null)
		{
			final BigInteger localAmount = localAmountView.getAmount();
			return localAmount != null ? WalletUtils.RBTValue(localAmount, exchangeRate.rate) : null;
		}
		else
		{
			return null;
		}
	}

	public boolean hasAmount()
	{
		return getAmount() != null;
	}

	private void update()
	{
		RBTAmountView.setEnabled(enabled);

		if (exchangeRate != null)
		{
			localAmountView.setEnabled(enabled);
			localAmountView.setCurrencySymbol(exchangeRate.currencyCode);

			if (exchangeDirection)
			{
				final BigInteger RBTAmount = RBTAmountView.getAmount();
				if (RBTAmount != null)
				{
					localAmountView.setAmount(null, false);
					localAmountView.setHint(WalletUtils.localValue(RBTAmount, exchangeRate.rate));
					RBTAmountView.setHint(null);
				}
			}
			else
			{
				final BigInteger localAmount = localAmountView.getAmount();
				if (localAmount != null)
				{
					RBTAmountView.setAmount(null, false);
					RBTAmountView.setHint(WalletUtils.RBTValue(localAmount, exchangeRate.rate));
					localAmountView.setHint(null);
				}
			}
		}
		else
		{
			localAmountView.setEnabled(false);
			localAmountView.setHint(null);
			RBTAmountView.setHint(null);
		}
	}

	public void setExchangeDirection(final boolean exchangeDirection)
	{
		this.exchangeDirection = exchangeDirection;

		update();
	}

	public boolean getExchangeDirection()
	{
		return exchangeDirection;
	}

	public View activeTextView()
	{
		if (exchangeDirection)
			return RBTAmountView.getTextView();
		else
			return localAmountView.getTextView();
	}

	public void requestFocus()
	{
		activeTextView().requestFocus();
	}

	public void setRBTAmount(@Nonnull final BigInteger amount)
	{
		final Listener listener = this.listener;
		this.listener = null;

		RBTAmountView.setAmount(amount, true);

		this.listener = listener;
	}

	public void setNextFocusId(final int nextFocusId)
	{
		RBTAmountView.setNextFocusId(nextFocusId);
		localAmountView.setNextFocusId(nextFocusId);
	}
}
