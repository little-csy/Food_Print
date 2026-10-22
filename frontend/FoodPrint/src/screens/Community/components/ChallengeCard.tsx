import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import type { GestureResponderEvent } from 'react-native';
import { CommunityChallenge } from '../types';

interface ChallengeCardProps {
  challenge: CommunityChallenge;
  onPress: (challenge: CommunityChallenge) => void;
  onJoinPress: (challengeId: string) => void;
}

export const ChallengeCard: React.FC<ChallengeCardProps> = ({
  challenge,
  onPress,
  onJoinPress,
}) => {
  const hasActiveRun = challenge.joined && !!challenge.userChallengeId;

  // UUID regex pattern to detect if type is a UUID (custom created)
  const isUUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(
    challenge.type || ''
  );

  // Get display text for type - hide UUID for custom challenges
  const getTypeDisplay = () => {
    if (hasActiveRun) {
      return `${challenge.progressPercent ?? 0}% complete`;
    }
    if (isUUID) {
      return 'Custom';
    }
    return challenge.type?.replace(/_/g, ' ') || 'Challenge';
  };

  return (
    <TouchableOpacity
      onPress={() => onPress(challenge)}
      style={[styles.card, { backgroundColor: challenge.color }]}
      activeOpacity={0.9}
    >
      <View style={styles.header}>
        <Text style={styles.icon}>{challenge.icon}</Text>
        <View style={styles.info}>
          <Text style={styles.title}>{challenge.title}</Text>
          <Text style={styles.status}>{challenge.duration}</Text>
          {hasActiveRun ? (
            <>
              <View style={styles.progressContainer}>
                <View style={styles.miniProgressBar}>
                  <View
                    style={[styles.miniProgressFill, { width: `${challenge.progressPercent ?? 0}%` }]}
                  />
                </View>
              </View>
              <Text style={styles.streak}>
                {challenge.completedDays ?? 0}/{Math.ceil((challenge.progressPercent ?? 1))} days {challenge.streak ? `🔥 ${challenge.streak}` : ''}
              </Text>
            </>
          ) : null}
        </View>
      </View>
      <Text style={styles.description} numberOfLines={3}>
        {challenge.description}
      </Text>
      <View style={styles.footer}>
        <Text style={styles.participants}>
          {getTypeDisplay()}
        </Text>
        <TouchableOpacity
          onPress={(e: GestureResponderEvent) => {
            e.stopPropagation();
            onJoinPress(challenge.id);
          }}
          style={[styles.joinButton, hasActiveRun && styles.joinedButton]}
        >
          <Text style={styles.joinText}>{hasActiveRun ? '✓ Active' : 'Join'}</Text>
        </TouchableOpacity>
      </View>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  card: {
    borderRadius: 20,
    justifyContent: 'space-between',
    marginBottom: 12,
    marginHorizontal: 8,
    padding: 16,
    width: 280,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 8,
    elevation: 4,
  },
  description: {
    color: '#FFF',
    fontSize: 13,
    lineHeight: 18,
    marginBottom: 12,
    opacity: 0.95,
  },
  footer: {
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  header: {
    flexDirection: 'row',
    marginBottom: 12,
  },
  icon: {
    fontSize: 40,
    marginRight: 12,
  },
  info: {
    flex: 1,
  },
  joinButton: {
    alignItems: 'center',
    backgroundColor: '#FFF',
    borderRadius: 20,
    elevation: 2,
    justifyContent: 'center',
    minWidth: 70,
    paddingHorizontal: 16,
    paddingVertical: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  joinedButton: {
    backgroundColor: 'rgba(255, 255, 255, 0.3)',
  },
  joinText: {
    color: '#333',
    fontSize: 13,
    fontWeight: '600',
  },
  miniProgressBar: {
    backgroundColor: 'rgba(255, 255, 255, 0.3)',
    borderRadius: 4,
    height: 6,
    marginVertical: 4,
    overflow: 'hidden',
    width: '100%',
  },
  miniProgressFill: {
    backgroundColor: '#FFF',
    height: '100%',
  },
  participants: {
    color: 'rgba(255, 255, 255, 0.85)',
    fontSize: 12,
    fontWeight: '500',
  },
  progressContainer: {
    marginVertical: 6,
  },
  status: {
    color: 'rgba(255, 255, 255, 0.8)',
    fontSize: 12,
    marginBottom: 2,
  },
  streak: {
    color: '#FFF',
    fontSize: 11,
    fontWeight: '600',
    marginTop: 2,
  },
  title: {
    color: '#FFF',
    fontSize: 16,
    fontWeight: '700',
    marginBottom: 2,
  },
});
