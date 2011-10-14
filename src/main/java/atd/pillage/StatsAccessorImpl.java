/*
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may
 *  not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package atd.pillage;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;


/**
 * Attaches to a StatsCollection and reports on all the counters, metrics, gauges, and labels.
 * Each report resets state, so counters are reported as deltas, and metrics distributions are
 * only tracked since the last report.
 */
class StatsAccessorImpl implements StatsAccessor {
    private StatsProvider provider;
    private List<StatsReporter> reporters = new ArrayList<StatsReporter>();
    private long lastSnap;
    private long currentSnap;

    private Map<String, Long> lastCounterMap = new HashMap<String, Long>();
    private Map<String, Long> deltaCounterMap = new HashMap<String, Long>();
    private Map<String, Distribution> lastMetricMap = new HashMap<String, Distribution>();
    private Map<String, Distribution> deltaMetricMap = new HashMap<String, Distribution>();
    
    public StatsAccessorImpl(StatsProvider provider){
    	this(provider, true);
    }
    
    public StatsAccessorImpl(StatsProvider provider, boolean startClean) {
    	this.provider = provider;
        if (startClean) {
            for (Map.Entry<String, Long> entry : this.provider.getCounters().entrySet()) {
                lastCounterMap.put(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, Distribution> entry : this.provider.getMetrics().entrySet()) {
                lastMetricMap.put(entry.getKey(), entry.getValue());
            }
        }
    }
    
    @Override
	public StatsSummary getFullSummary() {
		return provider.getSummary();
	}


	@Override
	public StatsSummary getDeltaSummary() {
		return new StatsSummary(deltaCounterMap, deltaMetricMap, provider.getLabels(), lastSnap, currentSnap);
	}


	@Override
	public void triggerSnap() {
		triggerCounterSnap();
		triggerMetricSnap();
		lastSnap = currentSnap;
		currentSnap = System.currentTimeMillis();
		StatsSummary deltas = getDeltaSummary();
		for( StatsReporter reporter : reporters){
			reporter.report(deltas);
		}
	}

	/**
	 * A snap reporter is essentially a snap listener. A reporter will 
	 * recieve a StatsSummary object.
	 */
	@Override
	public void addSnapReporter(StatsReporter reporter) {
		synchronized(this) {
			reporters.add(reporter);
		}		
	}

	@Override
	public void removeSnapReporter(StatsReporter reporter) {
		synchronized(this){
			reporters.remove(reporter);
		}
	}


    protected void triggerCounterSnap() {
        Map<String, Long> deltas = new HashMap<String, Long>();
        synchronized (this) {

            for (Map.Entry<String, Long> entry : provider.getCounters().entrySet()) {
                long lastValue = 0;
                if (lastCounterMap.containsKey(entry.getKey())) ;
                lastValue = lastCounterMap.get(entry.getKey());
                deltas.put(entry.getKey(), StatUtils.delta(lastValue, entry.getValue()));
                lastCounterMap.put(entry.getKey(), entry.getValue());
            }
        }
        deltaCounterMap = deltas;
    }

    public void triggerMetricSnap() {
        Map<String, Distribution> deltas = new HashMap<String, Distribution>();
        synchronized (this) {

            for (Map.Entry<String, Distribution> entry : provider.getMetrics().entrySet()) {

                if (lastMetricMap.containsKey(entry.getKey())) {
                    Distribution dist = lastMetricMap.get(entry.getKey());
                    deltas.put(entry.getKey(), entry.getValue().delta(dist));

                } else {
                    deltas.put(entry.getKey(), entry.getValue());
                }
                lastMetricMap.put(entry.getKey(), entry.getValue());
            }
        }
        deltaMetricMap = deltas;
    }
    

}
