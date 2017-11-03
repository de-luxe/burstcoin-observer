/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 by luxe - https://github.com/de-luxe - BURST-LUXE-RED2-G6JW-H4HG5
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package burstcoin.observer.controller;


import burstcoin.observer.ObserverProperties;
import burstcoin.observer.bean.NavigationBean;
import burstcoin.observer.bean.NavigationPoint;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BaseController
{
  protected void addNavigationBean(NavigationPoint active, Model model)
  {
    List<NavigationPoint> left = new ArrayList<>();
    left.add(NavigationPoint.NETWORK);
    left.add(NavigationPoint.POOL);
    left.add(NavigationPoint.NODE);
    left.add(NavigationPoint.ASSET);
    left.add(NavigationPoint.CROWDFUND);

    List<NavigationPoint> right = new ArrayList<>();
    right.add(NavigationPoint.API);
    right.add(NavigationPoint.FAUCET);
    right.add(NavigationPoint.FORUM);
    right.add(NavigationPoint.BLOCKEX);
    right.add(NavigationPoint.GITHUB);
    Collections.reverse(right);

    model.addAttribute("analyticsCode", ObserverProperties.getAnalyticsCode());
    model.addAttribute("navigation", new NavigationBean(left, right, active));
  }
}
